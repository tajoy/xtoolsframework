package x.tools.framework.event;

import android.text.TextUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.EventListener;
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
    private final Socket socket;
    private EventReader eventReader;
    private EventWriter eventWriter;
    private Thread threadReceive;

    EventBusClient(Socket socket) throws IOException {
        this.uuid = UUID.randomUUID().toString();
        this.socket = socket;
        this.eventReader = new EventReader(socket.getInputStream());
        this.eventWriter = new EventWriter(socket.getOutputStream());
        this.threadReceive = new Thread(this::receiveEvent);
        this.threadReceive.setName(this.toString() + "-thread");
        this.threadReceive.setDaemon(true);
        this.threadReceive.start();
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
        if (TextUtils.isEmpty(name))
            throw new IllegalArgumentException("event is empty");
        Event event = new Event(getId(), name, data);
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
        if (this.socket.isClosed())
            return;

        if (this.socket.isOutputShutdown())
            return;

        try {
            this.eventWriter.writeEvent(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void receiveEvent() {
        try {
            while (!this.socket.isClosed() && !this.socket.isInputShutdown()) {
                Event event = this.eventReader.readEvent();
                sendLocal(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return "EventBusClient-" + getId();
    }
}
