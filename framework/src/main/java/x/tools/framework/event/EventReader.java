package x.tools.framework.event;

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
        this.inputStream.notify();
    }

    public Event readEvent() {
        Event event = null;
        try {
            do {
                InputStream inputStream = this.inputStream.get();
                if (inputStream == null) {
                    this.inputStream.wait();
                    inputStream = this.inputStream.get();
                }

                try {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    event = (Event) ois.readObject();
                } catch (NullPointerException | IOException e) {
                    this.inputStream.set(null);
                    Thread.sleep(1000);
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
