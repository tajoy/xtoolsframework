package x.tools.eventbus;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class EventWriter {
    private final AtomicReference<OutputStream> outputStream = new AtomicReference<>(null);

    EventWriter() {
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream.set(outputStream);
    }

    public void writeEvent(Event event) {
        try {
            do {
                OutputStream outputStream = this.outputStream.get();
                if (outputStream == null) {
                    Thread.sleep(100);
                    continue;
                }

                try {
                    ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                    oos.writeObject(event);
                } catch (NullPointerException | IOException e) {
                    this.outputStream.set(null);
                    Thread.sleep(1000);
                    continue;
                }
                break;
            } while (!Thread.currentThread().isInterrupted());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
