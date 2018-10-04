package x.tools.eventbus;

import android.text.TextUtils;

import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import x.tools.eventbus.annotation.AnnotationError;
import x.tools.log.Loggable;
import x.tools.eventbus.annotation.AllEventSubscriber;
import x.tools.eventbus.annotation.ErrorSubscriber;
import x.tools.eventbus.annotation.EventSubscriber;
import x.tools.eventbus.annotation.OnSyncValueUpdate;
import x.tools.eventbus.annotation.SyncValue;
import x.tools.eventbus.annotation.ThreadMode;
import x.tools.eventbus.sync.AbstractSyncValue;
import x.tools.eventbus.sync.ISyncUpdateCallback;

class EventSubscriberWrapper implements Loggable {
    private interface OnError {
        void call(Throwable throwable);
    }

    private static class SubscriberInfo {
        private Method method;
        private String name;
        private String source;
        private ThreadMode mode;

        private SubscriberInfo(Method method, ThreadMode mode) {
            this.method = method;
            this.mode = mode;
        }

        private SubscriberInfo(Method method, String name, String source, ThreadMode mode) {
            this.method = method;
            this.name = name;
            this.source = source;
            this.mode = mode;
        }

        private boolean isFiltered(Event event) {
            if (!"*".equals(this.name) && !Objects.equals(event.getName(), this.name)) {
                return true;
            }
            if (!TextUtils.isEmpty(this.source)) {
                return !this.source.equals(event.getSource());
            }
            return false;
        }

        private void onEvent(Object s, Event event, OnError onError) {
            EventBus.callIn(mode, () -> {
                try {
                    method.invoke(s, event);
                } catch (Throwable t) {
                    onError.call(t);
                }
            });
        }

        private void onError(Object s, Event event, Throwable throwable, OnError onError) {
            EventBus.callIn(mode, () -> {
                try {
                    method.invoke(s, event, throwable);
                } catch (Throwable t) {
                    onError.call(t);
                }
            });
        }
    }

    private final AtomicReference<Object> subscriber;
    private final List<SubscriberInfo> allEventSubscriberList = new ArrayList<>();
    private final List<SubscriberInfo> errorEventSubscriberList = new ArrayList<>();
    private final List<SubscriberInfo> subscriberInfoList = new ArrayList<>();
    private final Map<String, AbstractSyncValue> syncValueMap = new HashMap<>();

    private static void checkMethod(Method method, Class... types) throws AnnotationError {
        Class[] parameterTypes = method.getParameterTypes();
        if (types.length != parameterTypes.length) {
            throw new AnnotationError(
                    String.format(
                            "expected %s, but got %s",
                            Arrays.toString(types),
                            Arrays.toString(parameterTypes)
                    )
            );
        }
        for (int i = 0; i < types.length; i++) {
            if (!ClassUtils.isAssignable(types[i], parameterTypes[i])) {
                throw new AnnotationError(
                        String.format(
                                "expected %s, but got %s",
                                Arrays.toString(types),
                                Arrays.toString(parameterTypes)
                        )
                );
            }
        }
    }

    private final IEventBus eventBus;

    private static Field[] getAllFields(Class cls) {
        List<Field> fieldList = new ArrayList<>();

        do {
            fieldList.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        } while (!cls.equals(Object.class));

        return fieldList.toArray(new Field[fieldList.size()]);
    }

    private static Method[] getAllMethods(Class cls) {
        List<Method> methodList = new ArrayList<>();
        do {
            methodList.addAll(Arrays.asList(cls.getDeclaredMethods()));
            cls = cls.getSuperclass();
        } while (!cls.equals(Object.class));

        return methodList.toArray(new Method[methodList.size()]);

    }


    EventSubscriberWrapper(IEventBus eventBus, Object subscriber) throws AnnotationError {
        this.eventBus = eventBus;
        this.subscriber = new AtomicReference<>(subscriber);
        Class cls = subscriber.getClass();

        Field[] fields = getAllFields(cls);
        Method[] methods = getAllMethods(cls);


        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof SyncValue) {
                    SyncValue sv = (SyncValue) annotation;
                    try {
                        Class fieldClass = field.getType();
                        if (!ClassUtils.isAssignable(fieldClass, AbstractSyncValue.class)) {
                            throw new AnnotationError(
                                    String.format(
                                            "expected %s, but got %s",
                                            AbstractSyncValue.class,
                                            fieldClass
                                    )
                            );
                        }
                        Constructor<? extends AbstractSyncValue> constructor = fieldClass.getConstructors()[0];
                        Class<?>[] parameterTypes = constructor.getParameterTypes();
                        Class dataClass = sv.type();
                        if (Object.class.equals(dataClass) && parameterTypes.length >= 2) {
                            dataClass = parameterTypes[2];
                        }
                        String id = sv.id();
                        Object data = null;
                        if (ClassUtils.isAssignable(dataClass, String.class)) {
                            data = sv.value();
                        } else {
                            try {
                                if (ClassUtils.isPrimitiveOrWrapper(dataClass)) {
                                    data = dataClass.getMethod("valueOf", String.class).invoke(dataClass, sv.value());
                                } else {
                                    data = EventBus.fromJson(sv.value(), dataClass);
                                }
                            } catch (Throwable ignore) {
                            }
                        }
                        if (TextUtils.isEmpty(id)) {
                            id = cls.getName() + "." + field.getName();
                        }
                        AbstractSyncValue syncValue = constructor.newInstance(eventBus, id, data);
                        syncValue.setValueClass(dataClass);
                        field.setAccessible(true);
                        field.set(subscriber, syncValue);
                        this.syncValueMap.put(field.getName(), syncValue);
                    } catch (Exception e) {
                        throw new AnnotationError(e);
                    }
                }
            }
        }

        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof AllEventSubscriber) {
                    checkMethod(method, Event.class);
                    allEventSubscriberList.add(
                            new SubscriberInfo(
                                    method,
                                    ((AllEventSubscriber) annotation).threadMode()
                            )
                    );
                }
                if (annotation instanceof ErrorSubscriber) {
                    checkMethod(method, Event.class, Throwable.class);
                    errorEventSubscriberList.add(
                            new SubscriberInfo(
                                    method,
                                    ((AllEventSubscriber) annotation).threadMode()
                            )
                    );
                }
                if (annotation instanceof EventSubscriber) {
                    checkMethod(method, Event.class);
                    EventSubscriber es = (EventSubscriber) annotation;
                    subscriberInfoList.add(
                            new SubscriberInfo(
                                    method,
                                    es.name(),
                                    es.source(),
                                    es.threadMode()
                            )
                    );
                }

                // SyncValue 处理完了, 监听注解才能处理
                if (annotation instanceof OnSyncValueUpdate) {
                    OnSyncValueUpdate onSyncValueUpdate = (OnSyncValueUpdate) annotation;
                    for (Map.Entry<String, AbstractSyncValue> entry : syncValueMap.entrySet()) {
                        Class fieldClass;
                        try {
                            fieldClass = cls.getField(entry.getKey()).getType();
                        } catch (NoSuchFieldException e) {
                            throw new AnnotationError(e);
                        }
                        if (!TextUtils.isEmpty(onSyncValueUpdate.field())
                                && !entry.getKey().equals(onSyncValueUpdate.field())) {
                            continue;
                        }
                        Constructor<? extends AbstractSyncValue> constructor = fieldClass.getConstructors()[0];
                        Class dataClass = constructor.getParameterTypes()[2];
                        checkMethod(method, AbstractSyncValue.class, dataClass, dataClass);
                        entry.getValue().setUpdateCallback(new ISyncUpdateCallback() {
                            @Override
                            public ThreadMode threadMode() {
                                return onSyncValueUpdate.threadMode();
                            }

                            @Override
                            public <T> void onUpdate(AbstractSyncValue<T> syncValue, T oldValue, T newValue) throws Throwable {
                                method.invoke(subscriber, syncValue, oldValue, newValue);
                            }
                        });
                    }
                }

            }
        }


        // 可能是后启动的客户端, 注册后需要立即同步最新值
        for (AbstractSyncValue syncValue : syncValueMap.values()) {
            syncValue.sync();
        }
    }

    public synchronized void onEvent(Event event) {
        Object s = subscriber.get();
        if (s == null) {
            eventBus.unsubscribe(this);
            return;
        }

        for (AbstractSyncValue syncValue : syncValueMap.values()) {
            EventBus.callIn(ThreadMode.ASYNC, () -> {
                if (syncValue.onEvent(event)) return;
            });
        }

        for (SubscriberInfo info : allEventSubscriberList) {
            info.onEvent(s, event, (t) -> onError(event, t));
        }

        for (SubscriberInfo info : subscriberInfoList) {
            if (info.isFiltered(event))
                continue;
            info.onEvent(s, event, (t) -> onError(event, t));
        }
    }

    public synchronized void onError(Event event, Throwable throwable) {
        Object s = subscriber.get();
        if (s == null) {
            eventBus.unsubscribe(this);
            return;
        }
        if (errorEventSubscriberList.size() > 0) {
            for (SubscriberInfo info : errorEventSubscriberList) {
                info.onError(
                        s,
                        event,
                        throwable,
                        (t) -> error(
                                t,
                                "Handle Error %s, %s got ERROR!",
                                event,
                                throwable
                        )
                );
            }
        } else {
            error(throwable, "Handle %s got ERROR!", event);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (Objects.equals(subscriber, o)) return true;
        if (Objects.equals(subscriber.get(), o)) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSubscriberWrapper that = (EventSubscriberWrapper) o;
        return Objects.equals(subscriber.get(), that.subscriber.get());
    }

    @Override
    public int hashCode() {
        if (subscriber.get() == null) {
            return subscriber.hashCode();
        }
        return subscriber.get().hashCode();
    }

    @Override
    public String toString() {
        return "EventSubscriberWrapper{" +
                "subscriber=" + String.valueOf(subscriber.get()) +
                ", allEventSubscriberList=" + allEventSubscriberList +
                ", errorEventSubscriberList=" + errorEventSubscriberList +
                ", subscriberInfoList=" + subscriberInfoList +
                '}';
    }
}
