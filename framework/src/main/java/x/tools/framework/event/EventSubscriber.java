package x.tools.framework.event;

import java.util.Objects;

class EventSubscriber {
    private final Object subscriber;

    EventSubscriber(Object subscriber) {
        this.subscriber = subscriber;
    }

    public void onEvent(Event event) {

    }

    public boolean isSubscriber(Object subscriber) {
        return this.subscriber.equals(subscriber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (Objects.equals(subscriber, o)) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventSubscriber that = (EventSubscriber) o;
        return Objects.equals(subscriber, that.subscriber);
    }

    @Override
    public int hashCode() {
        return subscriber.hashCode();
    }
}
