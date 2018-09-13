package x.tools.eventbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AllEventSubscriber {
    /**
     * 分发事件的线程模式, 参见: {@link ThreadMode}
     */
    ThreadMode threadMode() default ThreadMode.MAIN;
}
