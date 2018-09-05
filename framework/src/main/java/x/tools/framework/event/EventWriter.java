package x.tools.framework.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class EventWriter {
    private final AtomicReference<OutputStream> outputStream = new AtomicReference<>(null);
    EventWriter() {
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream.set(outputStream);
        this.outputStream.notify();
    }

    public void writeEvent(Event event) throws IOException {
        try {
            do {
                OutputStream outputStream = this.outputStream.get();
                if (outputStream == null) {
                    this.outputStream.wait();
                    outputStream = this.outputStream.get();
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
