package x.tools.framework.event;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import x.tools.framework.error.AnnotationError;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;
import static x.tools.framework.XUtils.getProcessName;

public class EventBusClient implements EventBus, Closeable {
    protected final Set<EventSubscriberWrapper> subscribers = synchronizedSet(
            newSetFromMap(
                    new WeakHashMap<>()
            )
    );
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
        this.processName = getProcessName();
        this.address = new LocalSocketAddress(address);
        this.eventReader = new EventReader();
        this.eventWriter = new EventWriter();
        this.connecting = new Thread(this::runConnecting);
        this.connecting.setName("connecting-thread -" + this.toString());
        this.connecting.setDaemon(true);
        this.connecting.start();

        this.receiving = new Thread(this::runReceiving);
        this.receiving.setName("receiving-thread -" + this.toString());
        this.receiving.setDaemon(true);
        this.receiving.start();

        this.sending = new Thread(this::runSending);
        this.sending.setName("sending-thread -" + this.toString());
        this.sending.setDaemon(true);
        this.sending.start();
    }

    public String getId() {
        return processName + "-" + uuid;
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
                    GlobalEventBus.toJson(data)
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
            subscribers.add(new EventSubscriberWrapper(subscriber));
        } catch (AnnotationError annotationError) {
            annotationError.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(Object subscriber) {
        subscribers.remove(subscriber);
    }

    private void sendLocal(Event event) {
        synchronized (subscribers) {
            for (EventSubscriberWrapper eventSubscriberWrapper : subscribers) {
                eventSubscriberWrapper.onEvent(this, event);
            }
        }
    }

    private void sendRemote(Event event) {
        sendingQueue.offer(event);
    }

    private void runConnecting() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                LocalSocket socket = new LocalSocket();
                socket.connect(this.address);
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
