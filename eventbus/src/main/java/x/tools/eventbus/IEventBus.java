package x.tools.eventbus;


import x.tools.eventbus.annotation.AllEventSubscriber;
import x.tools.eventbus.annotation.ErrorSubscriber;
import x.tools.eventbus.annotation.EventSubscriber;

public interface IEventBus extends IEventSender {

    /**
     * 添加事件拦截器, 监听器可以拦截事件的分发
     * 参见: {@link IEventInterpolator}
     *
     * @param interpolator 事件监听器
     */
    void addInterpolator(IEventInterpolator interpolator);

    /**
     * 移除事件拦截器
     *
     * @param interpolator 事件监听器
     */
    void removeInterpolator(IEventInterpolator interpolator);

    /**
     * 订阅事件, 订阅对象使用注解来标注需要接受的事件的方法
     * 参见: {@link AllEventSubscriber}, {@link EventSubscriber}, {@link ErrorSubscriber}
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
