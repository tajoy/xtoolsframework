package x.tools.framework.event;


public interface IEventBus extends IEventSender {

    /**
     * 添加事件监听器, 监听器可以拦截事件的分发
     * 参见: IEventListener
     *
     * @param listener 事件监听器
     */
    void addListener(IEventListener listener);

    /**
     * 移除事件监听器
     *
     * @param listener 事件监听器
     */
    void removeListener(IEventListener listener);

    /**
     * 订阅事件, 订阅对象使用注解来标注需要接受的事件的方法
     * 参见: AllEventSubscriber, EventSubscriber, ErrorSubscriber
     *
     * @param subscriber 订阅事件的对象
     */
    void subscribe(Object subscriber);

    /**
     * 取消订阅
     *
     * @param subscriber 订阅事件的对象
     */
    void unsubscribe(Object subscriber);

}
