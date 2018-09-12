package x.tools.framework.event.annotation;

public enum ThreadMode {
    /**
     * 订阅者将被直接调用, 该线程是触发事件的线程, 如果是远程事件, 将会在接收事件的网络
     * 线程中调用.
     */
    POSTING,

    /**
     * 订阅者将总会被异步调用, 将不会被触发事件和主线程内调用.
     * 所有该模式下的调用将在同一个异步线程内.
     */
    ASYNC,

    /**
     * 将总会被在主线程内调用, 如果触发事件是在主线程, 将会下再一次主线程循环内调用.
     */
    MAIN
}
