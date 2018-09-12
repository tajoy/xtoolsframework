package x.tools.eventbus.sync;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import x.tools.eventbus.Event;
import x.tools.eventbus.EventBus;
import x.tools.eventbus.IEventBus;

public abstract class AbstractSyncValue<T> {
    private static final String TYPE_SYNC = "sync";
    private static final String TYPE_CHANGED = "changed";

    private static final String KEY_TYPE = "type";
    private static final String KEY_WHEN = "when";
    private static final String KEY_FROM = "from";
    private static final String KEY_TO = "to";

    protected final String id;
    protected final IEventBus eventBus;
    protected Timestamp lastTimeModified = now();
    protected T value;
    private ISyncUpdateCallback updateCallback = null;

    public AbstractSyncValue(IEventBus eventBus, String id, T value) {
        this.eventBus = eventBus;
        this.value = value;
        this.id = id;
    }

    public T getValue() {
        synchronized (this) {
            return value;
        }
    }

    protected abstract void putValue(JSONObject jsonObject, String key, T value) throws JSONException;

    protected abstract T getValue(JSONObject jsonObject, String key) throws JSONException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSyncValue)) return false;
        AbstractSyncValue<?> syncValue = (AbstractSyncValue<?>) o;
        return Objects.equals(id, syncValue.id) &&
                Objects.equals(lastTimeModified, syncValue.lastTimeModified) &&
                Objects.equals(getValue(), syncValue.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastTimeModified, getValue());
    }

    public void setUpdateCallback(ISyncUpdateCallback updateCallback) {
        this.updateCallback = updateCallback;
    }

    private void onUpdate(T oldValue, T newValue) {
        if (this.updateCallback != null) {
            EventBus.callIn(this.updateCallback.threadMode(), () -> {
                try {
                    this.updateCallback.onUpdate(this, oldValue, newValue);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private static Timestamp now() {
        long timeInMillis = System.currentTimeMillis();
        long timeInNanos = System.nanoTime();
        Timestamp timestamp = new Timestamp(timeInMillis);
        timestamp.setNanos((int) (timeInNanos % 1000000000));
        return timestamp;
    }

    private static long DEFAULT_TIMEOUT = 1000;
    private static long DEFAULT_INTERVAL = 50;


    /**
     * 尝试设置新的值, 设置过程是异步的, 并不一定会马上生效, 也有可能设置失败
     *
     * @param value 要设置的值
     */
    public void trySetValue(T value) {
        synchronized (this) {
            if (Objects.equals(this.value, value))
                return;
        }
        JSONObject jsonObject = new JSONObject();
        Timestamp now = now();
        try {
            jsonObject.put(KEY_TYPE, TYPE_CHANGED);
            jsonObject.put(KEY_WHEN, now.toString());
            putValue(jsonObject, KEY_TO, value);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        synchronized (this) {
            try {
                putValue(jsonObject, KEY_FROM, this.value);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        this.eventBus.triggerRaw(this.id, jsonObject);
    }

    /**
     * 设置为新的值, 并等待同步到该值
     *
     * @param value        要设置的值
     * @param timeoutMills 等待同步的超时时间, 毫秒
     * @throws TimeoutException 在一定时间内设置失败
     */
    public void setValue(T value, long timeoutMills) throws TimeoutException, InterruptedException {
        long now = System.currentTimeMillis();
        long begin = now;
        do {
            now = System.currentTimeMillis();
            if (now > begin + timeoutMills) {
                throw new TimeoutException();
            }
            trySetValue(value);
            Thread.sleep(DEFAULT_INTERVAL);
        } while (!Objects.equals(value, getValue()));
    }

    /**
     * 设置为新的值, 并等待同步到该值, 默认等待 1 秒
     *
     * @param value 要设置的值
     * @throws TimeoutException 在一定时间内设置失败
     */
    public void setValue(T value) throws TimeoutException, InterruptedException {
        setValue(value, DEFAULT_TIMEOUT);
    }

    /**
     * 强制设置为新的值, 并等待同步到该值, 无等待时间限制
     * !!! 请谨慎使用
     *
     * @param value 要设置的值
     */
    public void setValueForce(T value) {
        while (!Objects.equals(value, getValue())) {
            trySetValue(value);
            try {
                Thread.sleep(DEFAULT_INTERVAL);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public boolean onEvent(Event event) {
        if (!event.getName().equals(this.id)) return false;
        boolean isLocal = Objects.equals(event.getSource(), this.eventBus.getId());
        boolean isMaster = EventBus.isServer();

        T from, to;
        Timestamp when;
        JSONObject jsonObject;
        String type;
        try {
            jsonObject = event.getData();
            type = jsonObject.getString(KEY_TYPE);
            from = getValue(jsonObject, KEY_FROM);
            to = getValue(jsonObject, KEY_TO);
            when = Timestamp.valueOf(jsonObject.getString(KEY_WHEN));
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }

        if (isMaster) {
            // 主值, 负责管理是否更新
            if (isLocal) {
                // 本地更新, 直接更新
                if (TYPE_CHANGED.equals(type)) {
                    synchronized (this) {
                        this.lastTimeModified = when;
                        this.value = to;
                    }
                    onUpdate(from, to);
                }
            } else {
                if (TYPE_CHANGED.equals(type)) {
                    // 远端请求更新, 比较条件进行更新
                    synchronized (this) {
                        if (this.lastTimeModified.after(when)) {
                            return true;
                        }
                        if (!Objects.equals(this.value, from)) {
                            return true;
                        }
                        this.lastTimeModified = when;
                        this.value = to;
                    }
                    onUpdate(from, to);
                    try {
                        jsonObject = new JSONObject(jsonObject.toString());
                        jsonObject.put(KEY_TYPE, TYPE_SYNC);
                        // 通知从值同步
                        this.eventBus.triggerRaw(this.id, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            // 从值, 负责同步和请求主值更新
            if (isLocal) {
                // 本地更新, 请求到远端后决定是否更新
                return true;
            } else {
                // 从主值发送来的同步通知
                synchronized (this) {
                    this.lastTimeModified = when;
                    this.value = to;
                }
                onUpdate(from, to);
            }
        }

        return true;
    }
}
