package x.tools.framework.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EventSubscriber {

    /**
     * 需要匹配的事件名称
     */
    String name();

    /**
     * 需要匹配的事件源, 一般是进程ID. 空值代表匹配所有.
     */
    String source() default "";

    /**
     * 分发事件的线程模式, 参见: {@link ThreadMode}
     */
    ThreadMode threadMode() default ThreadMode.POSTING;
}
