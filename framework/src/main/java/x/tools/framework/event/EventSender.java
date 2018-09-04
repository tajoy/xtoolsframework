package x.tools.framework.event;

public interface EventSender {
    void trigger(String name);
    void trigger(String name, Object data);
}
