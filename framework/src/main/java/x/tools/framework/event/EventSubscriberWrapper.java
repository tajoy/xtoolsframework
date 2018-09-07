package x.tools.framework.event;

import android.text.TextUtils;

import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import x.tools.framework.error.AnnotationError;
import x.tools.framework.event.annotation.AllEventSubscriber;
import x.tools.framework.event.annotation.ErrorSubscriber;
import x.tools.framework.event.annotation.EventSubscriber;
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
            if (!Objects.equals(event.getName(), this.name)) {
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

    EventSubscriberWrapper(Object subscriber) throws AnnotationError {
        this.subscriber = new AtomicReference<>(subscriber);
        Class cls = subscriber.getClass();
        Method[] methods = cls.getDeclaredMethods();
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
            }
        }
    }

    public void onEvent(EventBus eventBus, Event event) {
        Object s = subscriber.get();
        if (s == null) {
            eventBus.unsubscribe(this);
            return;
        }

        for (Method method : allEventSubscriberList) {
            try {
                method.invoke(s, event);
            } catch (Throwable t) {
                onError(eventBus, event, t);
            }
        }

        for (SubscriberInfo info : subscriberInfoList) {
            try {
                if (info.isFiltered(event))
                    continue;
                info.onEvent(s, event);
            } catch (Throwable t) {
                onError(eventBus, event, t);
            }
        }
    }

    public void onError(EventBus eventBus, Event event, Throwable throwable) {
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
