package x.tools.eventbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对本类字段的 {@link SyncValue} 的值的更新进行监听
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface OnSyncValueUpdate {

    /**
     * 要监听的 SyncValue 的字段名, 默认监听所有
     */
    String field() default "";


    /**
     * 分发事件的线程模式, 参见: {@link ThreadMode}
     */
    ThreadMode threadMode() default ThreadMode.MAIN;
}
