package x.tools.framework.event;


public interface IEventBus extends IEventSender {

    /**
     * Subscribe for event
     *
     * @param subscriber The subscriber to subscribe
     */
    void subscribe(Object subscriber);

    /**
     * Unsubscribe all listeners of an event
     *
     * @param subscriber the subscriber to unsubscribe
     */
    void unsubscribe(Object subscriber);

}
