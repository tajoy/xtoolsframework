package x.tools.framework.event;

import org.json.JSONObject;

public interface EventSender {
    void trigger(String name);
    void trigger(String name, Object data);
    void triggerRaw(String name, JSONObject data);
}
