package x.tools.eventbus;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import x.tools.eventbus.annotation.AnnotationError;
import x.tools.eventbus.annotation.ThreadMode;
import x.tools.eventbus.log.Loggable;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;

public class EventBusClient implements IEventBus, Closeable, Loggable {
    private final Set<EventSubscriberWrapper> subscribers = synchronizedSet(
            newSetFromMap(
                    new HashMap<>()
            )
    );

    private static class EventListenerComparator implements Comparator<IEventInterpolator> {
        @Override
        public int compare(IEventInterpolator o1, IEventInterpolator o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    }

    private final SortedSet<IEventInterpolator> interpolators = new ConcurrentSkipListSet<>(new EventListenerComparator());

    private final String uuid;
    private final String processName;
    private EventReader eventReader;
    private EventWriter eventWriter;
    private LocalSocketAddress address;
    private LocalSocket socket;
    private Thread connecting;
    private Thread receiving;
    private Thread sending;

    private final LinkedBlockingQueue<Event> sendingQueue = new LinkedBlockingQueue<>();

    EventBusClient(String address) {
        this.uuid = UUID.randomUUID().toString();
        this.processName = EventBus.getProcessName();
        if (address != null) {
            this.address = new LocalSocketAddress(address);
            this.connecting = new Thread(this::runConnecting);
            this.connecting.setName("connecting-thread -" + this.toString());
            this.connecting.setDaemon(true);
            this.connecting.start();
            this.eventReader = new EventReader();
            this.eventWriter = new EventWriter();

            this.receiving = new Thread(this::runReceiving);
            this.receiving.setName("receiving-thread -" + this.toString());
            this.receiving.setDaemon(true);
            this.receiving.start();

            this.sending = new Thread(this::runSending);
            this.sending.setName("sending-thread -" + this.toString());
            this.sending.setDaemon(true);
            this.sending.start();
        }
    }

    @Override
    public String getId() {
        return processName + "-" + uuid;
    }

    @Override
    public void trigger(Event event) {
        sendLocal(event);
        sendRemote(event);
    }

    @Override
    public void trigger(String name) {
        trigger(name, null);
    }

    @Override
    public void trigger(String name, Object data) {
        Event event;
        if (data == null) {
            event = new Event(
                    name,
                    getId(),
                    null,
                    null
            );
        } else {
            event = new Event(
                    name,
                    getId(),
                    data.getClass().toString(),
                    EventBus.toJson(data)
            );
        }
        sendLocal(event);
        sendRemote(event);
    }

    @Override
    public void triggerRaw(String name, JSONObject data) {
        Event event;
        if (data == null) {
            event = new Event(
                    name,
                    getId(),
                    null,
                    null
            );
        } else {
            event = new Event(
                    name,
                    getId(),
                    null,
                    data.toString()
            );
        }
        sendLocal(event);
        sendRemote(event);
    }


    @Override
    public void subscribe(Object subscriber) {
        try {
            subscribers.add(new EventSubscriberWrapper(this, subscriber));
        } catch (AnnotationError annotationError) {
            annotationError.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        subscribers.remove(subscriber);
    }

    public void addInterpolator(IEventInterpolator listener) {
        interpolators.add(listener);
    }

    public void removeInterpolator(IEventInterpolator listener) {
        interpolators.remove(listener);
    }

    private void sendLocal(Event event) {
        EventBus.callIn(ThreadMode.ASYNC, () -> {
            synchronized (interpolators) {
                for (IEventInterpolator interpolator : interpolators) {
                    try {
                        if (interpolator.onEvent(event)) return;
                    } catch (Throwable t) {
                        error(t, "error when sendLocal, interpolator: %s, event: %s", interpolator, event);
                    }
                }
            }
            synchronized (subscribers) {
                for (EventSubscriberWrapper eventSubscriberWrapper : subscribers) {
                    try {
                        eventSubscriberWrapper.onEvent(event);
                    } catch (Throwable t) {
                        error(t, "error when sendLocal, eventSubscriberWrapper: %s, event: %s", eventSubscriberWrapper, event);
                    }
                }
            }
        });
    }

    private void sendRemote(Event event) {
        if (this.address != null) {
            sendingQueue.offer(event);
        }
    }

    private void runConnecting() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                LocalSocket socket = new LocalSocket();
                debug("client connecting to: %s", this.address.getName());
                socket.connect(this.address);
                debug("client connected!");
                this.eventWriter.setOutputStream(socket.getOutputStream());
                this.eventReader.setInputStream(socket.getInputStream());
                this.socket = socket;
            } catch (IOException ignore) {
                try {
                    Thread.sleep(1000);
                    continue;
                } catch (InterruptedException ignore1) {
                    break;
                }
            }
            break;
        }
        this.connecting = null;
        debug("connecting finish!");
    }

    private void runSending() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = sendingQueue.take();
                this.eventWriter.writeEvent(event);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void runReceiving() {
        while (!Thread.currentThread().isInterrupted()) {
            Event event = this.eventReader.readEvent();
            if (event != null) {
                sendLocal(event);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.sending.interrupt();
        this.receiving.interrupt();

        if (this.connecting != null)
            this.connecting.interrupt();

        if (this.socket != null)
            this.socket.close();

    }


    @Override
    public String toString() {
        return "EventBusClient-" + getId();
    }
}
