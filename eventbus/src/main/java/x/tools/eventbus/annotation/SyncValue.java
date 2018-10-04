package x.tools.eventbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import x.tools.eventbus.sync.AbstractSyncValue;


/**
 * 该注解用于字段上, 字段类型为 {@link AbstractSyncValue} 的子类
 * 自动生成 ID, 并添加到事件总线上接收远端同步事件
 * {@link AbstractSyncValue} 设置时自动根据 ID 发送同步事件
 * 只支持常用类型: boolean, byte, int, long, float, double, string
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SyncValue {

    /**
     * 用于标识全局同步变量的 ID, 任何相同ID 的字段都会同步更新
     *
     * 默认自动生成: 全类名 + "." + 字段名
     */
    String id() default "";


    /**
     * 用于标识初始化值的生成器
     * 初始化时将会调用数据类型的 valueOf 方法来初始化同步变量
     * type 不为默认值时, 使用 EventBus.fromJson(value, type)来初始化
     */
    String value() default "";

    /**
     * 用于指定序列化的值的类型
     */
    Class<?> type() default Object.class;
}

