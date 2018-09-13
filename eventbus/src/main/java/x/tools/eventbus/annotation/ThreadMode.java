package x.tools.eventbus.annotation;

public enum ThreadMode {
    /**
     * 订阅者将总会被异步调用, 将不会在正在执行的线程内被调用.
     * 这意味着, 该模式下, 永远是异步, 并且不总是在同一个线程内执行
     */
    ASYNC,

    /**
     * 将总会被在主线程内调用, 如果触发事件是在主线程, 将会在下一次主线程循环内调用.
     */
    MAIN
}
