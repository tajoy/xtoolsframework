package x.tools.eventbus;

public interface IEventInterpolator extends Comparable<IEventInterpolator> {

    /**
     * 决定时间处理器优先级排序, 值越高, 越先处理
     * @return 优先值
     */
    default int priority() {
        return 0;
    }

    @Override
    default int compareTo(IEventInterpolator o) {
        if (this.priority() > o.priority()) {
            return -1;
        } else if (this.priority() == o.priority()) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * 响应事件的回调, 可截获
     * @param event 发生的事件
     * @return 返回一个布尔值决定是否该事件已被处理不在分发,
     *      true: 已处理,不再分发
     *      false: 未处理,继续分发
     */
    boolean onEvent(Event event);
}
