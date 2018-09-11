package x.tools.framework.event;

import android.text.TextUtils;

import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import x.tools.framework.error.AnnotationError;
import x.tools.framework.event.annotation.AllEventSubscriber;
import x.tools.framework.event.annotation.ErrorSubscriber;
import x.tools.framework.event.annotation.EventSubscriber;
import x.tools.framework.event.annotation.OnSyncValueUpdate;
import x.tools.framework.event.annotation.SyncValue;
import x.tools.framework.event.sync.AbstractSyncValue;
import x.tools.framework.event.sync.ISyncUpdateCallback;
import x.tools.framework.log.Loggable;

class EventSubscriberWrapper implements Loggable {
    private static class SubscriberInfo {
        private Method method;
        private String name;
        private String source;

        private SubscriberInfo(Method method, String name, String source) {
            this.method = method;
            this.name = name;
            this.source = source;
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

        private void onEvent(Object s, Event event) throws Throwable {
            method.invoke(s, event);
        }
    }

    private final AtomicReference<Object> subscriber;
    private final List<Method> allEventSubscriberList = new ArrayList<>();
    private final List<Method> errorEventSubscriberList = new ArrayList<>();
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

    EventSubscriberWrapper(IEventBus eventBus, Object subscriber) throws AnnotationError {
        this.eventBus = eventBus;
        this.subscriber = new AtomicReference<>(subscriber);
        Class cls = subscriber.getClass();

        Field[] fields = cls.getFields();
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
                        Class dataClass = constructor.getParameterTypes()[3];
                        String id = sv.id();
                        Object data;
                        try {
                            if (ClassUtils.isAssignable(dataClass, String.class)) {
                                data = sv.value();
                            } else {
                                data = dataClass.getMethod("valueOf", String.class).invoke(dataClass, sv.value());
                            }
                        } catch (Exception e) {
                            throw new AnnotationError(e);
                        }

                        if (TextUtils.isEmpty(id)) {
                            id = cls.getName() + "." + field.getName();
                        }
                        AbstractSyncValue syncValue = constructor.newInstance(eventBus, id, data);
                        field.set(subscriber, syncValue);
                        this.syncValueMap.put(field.getName(), syncValue);
                    } catch (Exception e) {
                        throw new AnnotationError(e);
                    }
                }
            }
        }

        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof AllEventSubscriber) {
                    checkMethod(method, Event.class);
                    allEventSubscriberList.add(method);
                }
                if (annotation instanceof ErrorSubscriber) {
                    checkMethod(method, Event.class, Throwable.class);
                    errorEventSubscriberList.add(method);
                }
                if (annotation instanceof EventSubscriber) {
                    checkMethod(method, Event.class);
                    EventSubscriber es = (EventSubscriber) annotation;
                    subscriberInfoList.add(new SubscriberInfo(method, es.name(), es.source()));
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
                        Class dataClass = constructor.getParameterTypes()[3];
                        checkMethod(method, AbstractSyncValue.class, dataClass, dataClass);
                        entry.getValue().setUpdateCallback(new ISyncUpdateCallback() {
                            @Override
                            public <T> void onUpdate(AbstractSyncValue<T> syncValue, T oldValue, T newValue) throws Throwable {
                                method.invoke(subscriber, syncValue, oldValue, newValue);
                            }
                        });
                    }
                }

            }
        }

    }

    public synchronized void onEvent(Event event) {
        Object s = subscriber.get();
        if (s == null) {
            eventBus.unsubscribe(this);
            return;
        }

        for (AbstractSyncValue syncValue : syncValueMap.values()) {
            if (syncValue.onEvent(event)) return;
        }

        for (Method method : allEventSubscriberList) {
            try {
                method.invoke(s, event);
            } catch (Throwable t) {
                onError(event, t);
            }
        }

        for (SubscriberInfo info : subscriberInfoList) {
            try {
                if (info.isFiltered(event))
                    continue;
                info.onEvent(s, event);
            } catch (Throwable t) {
                onError(event, t);
            }
        }
    }

    public synchronized void onError(Event event, Throwable throwable) {
        Object s = subscriber.get();
        if (s == null) {
            eventBus.unsubscribe(this);
            return;
        }
        if (errorEventSubscriberList.size() > 0) {
            for (Method method : errorEventSubscriberList) {
                try {
                    method.invoke(s, event, throwable);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
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
