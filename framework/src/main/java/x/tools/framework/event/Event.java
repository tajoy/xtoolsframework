package x.tools.framework.event;

import android.text.TextUtils;

import java.util.UUID;

public class Event {

    private final String id = UUID.randomUUID().toString();
    private final String source;
    private final String name;
    private final long timestamp = System.currentTimeMillis();
    private final Object data;

    public Event(String source, String name) {
        this(source, name, null);
    }

    public Event(String source, String name, Object data) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Event name is empty");
        }
        this.source = source;
        this.name = name;
        this.data = data;
    }


    public boolean isUserEvent() {
        return !name.startsWith("eventbus.");
    }


    public String getSource() {
        return source;
    }


    public String getName() {
        return name;
    }


    public long getTimestamp() {
        return timestamp;
    }


    public Object getData() {
        return data;
    }


    public <T> T getData(Class<T> type) {
        return type.cast(getData());
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
        sb.append(", name='").append(name).append('\'');
        sb.append(", source=").append(source);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
