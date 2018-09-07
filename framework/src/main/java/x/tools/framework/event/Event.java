package x.tools.framework.event;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Event implements java.io.Serializable {
    private static final String TAG = Event.class.getSimpleName();

    private String name = "*";
    private String id = UUID.randomUUID().toString();
    private long timestamp = System.currentTimeMillis();
    private String source = "unknown";
    private String typeName;
    private String preload;

    public Event() {
    }

    public Event(String name, String source) {
        if (TextUtils.isEmpty(name)) {
            this.name = "*";
        }
        this.name = name;
        this.source = source;
    }

    public Event(String name, String source, String typeName, String preload) {
        if (TextUtils.isEmpty(name)) {
            this.name = "*";
        }
        this.name = name;
        this.source = source;
        this.typeName = typeName;
        this.preload = preload;
    }

    public Event(String name, String id, long timestamp, String source, String typeName, String preload) {
        if (TextUtils.isEmpty(name)) {
            this.name = "*";
        }
        this.name = name;
        this.id = id;
        this.timestamp = timestamp;
        this.source = source;
        this.typeName = typeName;
        this.preload = preload;
    }

    public boolean isUserEvent() {
        return !this.name.startsWith("eventbus.");
    }

    public boolean isDefault() {
        return "*".equals(this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getPreload() {
        return preload;
    }

    public void setPreload(String preload) {
        this.preload = preload;
    }

    public JSONObject getData() throws JSONException {
        return new JSONObject(preload);
    }

    public Object getData(ClassLoader classLoader) {
        if (getTypeName() == null)
            return null;
        try {
            return GlobalEventBus.fromJson(getPreload(), classLoader.loadClass(getTypeName()));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public <T> T getData(Class<T> type) {
        if (type == null) throw new AssertionError("type == null");
        if (getPreload() == null) {
            return null;
        }
        if (getTypeName() != null && !type.getName().equals(getTypeName())) {
            Log.w(TAG, "typeName != className: " + getTypeName() + " != " + type.getName());
        }
        return GlobalEventBus.fromJson(getPreload(), type);
    }


    public <T> T getData(Class<T> type, T defaultValue) {
        T t = getData(type);
        return t == null ? defaultValue : t;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Event{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append("\'");
        sb.append(", source=").append(source);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", typeName=").append(typeName);
        sb.append(", preload=\'").append(preload);
        sb.append("\'}");
        return sb.toString();
    }
}
