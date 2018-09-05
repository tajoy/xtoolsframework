package x.tools.framework.event;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;

public class EventBusClient implements EventBus {
    protected final Set<EventSubscriber> subscribers = synchronizedSet(
            newSetFromMap(
                    new WeakHashMap<>()
            )
    );
    private final String uuid;
    private LocalSocket socket;
    private EventReader eventReader;
    private EventWriter eventWriter;
    private LocalSocketAddress address;
    private Thread connecting;
    private Thread receiving;
    private Thread sending;

    EventBusClient(String address) throws IOException {
        this.uuid = UUID.randomUUID().toString();
        this.address = new LocalSocketAddress(address);
        this.eventReader = new EventReader();
        this.eventWriter = new EventWriter();
        this.connecting = new Thread(this::runConnecting);
        this.connecting.setName("receiving-thread -" + this.toString());
        this.connecting.setDaemon(true);
        this.connecting.start();

        this.receiving = new Thread(this::runReceiving);
        this.receiving.setName("receiving-thread -" + this.toString());
        this.receiving.setDaemon(true);
        this.receiving.start();

        this.receiving = new Thread(this::runReceiving);
        this.receiving.setName("receiving-thread -" + this.toString());
        this.receiving.setDaemon(true);
        this.receiving.start();
    }

    public String getId() {
        return uuid;
    }

    @Override
    public void trigger(String name) {
        trigger(name, null);
    }

    @Override
    public void trigger(String name, Object data) {
        Event event = new Event(
                name,
                getId(),
                data.getClass().toString(),
                GlobalEventBus.toJson(data)
        );
        sendLocal(event);
        sendRemote(event);
    }


    @Override
    public void subscribe(Object subscriber) {
        subscribers.add(new EventSubscriber(subscriber));
    }

    @Override
    public void unsubscribe(Object subscriber) {
        subscribers.remove(subscriber);
    }

    void sendLocal(Event event) {
        synchronized (subscribers) {
            Iterator<EventSubscriber> iterator = subscribers.iterator();
            while (iterator.hasNext()) {
                EventSubscriber eventSubscriber = iterator.next();
                eventSubscriber.onEvent(event);
            }
        }
    }

    void sendRemote(Event event) {
        try {
            this.eventWriter.writeEvent(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void runConnecting() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.socket = new LocalSocket();
                this.socket.connect(this.address);
                this.eventWriter.setOutputStream(this.socket.getOutputStream());
                this.eventReader.setInputStream(this.socket.getInputStream());
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

    void runSending() {
        while (!Thread.currentThread().isInterrupted()) {
            
        }
    }

    void runReceiving() {
        while (!Thread.currentThread().isInterrupted()) {
            Event event = this.eventReader.readEvent();
            if (event != null) {
                sendLocal(event);
            }
        }
    }


    @Override
    public String toString() {
        return "EventBusClient-" + getId();
    }
}
