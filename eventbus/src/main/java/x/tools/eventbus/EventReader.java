package x.tools.eventbus;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicReference;


public class EventReader {
    private final AtomicReference<InputStream> inputStream = new AtomicReference<>(null);


    EventReader() {
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream.set(inputStream);
    }

    public Event readEvent() {
        Event event = null;
        try {
            do {
                InputStream inputStream = this.inputStream.get();
                if (inputStream == null) {
                    Thread.sleep(100);
                    continue;
                }
                try {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    event = (Event) ois.readObject();
                } catch (NullPointerException | IOException e) {
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
