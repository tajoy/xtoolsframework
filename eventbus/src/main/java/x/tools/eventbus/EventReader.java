package x.tools.eventbus;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicReference;

import x.tools.log.Loggable;


public class EventReader implements Loggable {
    private final AtomicReference<InputStream> inputStream = new AtomicReference<>(null);


    EventReader() {
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream.set(inputStream);
    }

    public InputStream getInputStream() {
        return this.inputStream.get();
    }

    public Event readEvent() {
        Event event = null;
        try {
            do {
                InputStream inputStream = this.inputStream.get();
                if (inputStream == null) {
                    return null;
                }
                try {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    event = (Event) ois.readObject();
                } catch (NotSerializableException e) {
                    error(e, "error when readObject");
                    continue;
                } catch (IOException e) {
                    error(e, "error when readObject");
                    try {
                        inputStream.close();
                    } catch (IOException ignore) {
                    }
                    Thread.sleep(100);
                    this.inputStream.set(null);
                    continue;
                } catch (ClassCastException | ClassNotFoundException e) {
                    return null;
                }
                break;
            } while (!Thread.currentThread().isInterrupted());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return event;
    }

}
