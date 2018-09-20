package x.tools.eventbus;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import x.tools.log.Loggable;

public class EventWriter implements Loggable {
    private final AtomicReference<OutputStream> outputStream = new AtomicReference<>(null);

    EventWriter() {
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream.set(outputStream);
    }

    public OutputStream getOutputStream() {
        return this.outputStream.get();
    }

    public void writeEvent(Event event) {
        try {
            do {
                OutputStream outputStream = this.outputStream.get();
                if (outputStream == null) {
                    debug("outputStream == null, wait 100ms");
                    Thread.sleep(100);
                    continue;
                }

                try {
                    ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                    oos.writeObject(event);
                } catch (NotSerializableException e) {
                    error(e, "error when writeObject: %s", event);
                } catch (IOException e) {
                    error(e, "error when writeObject: %s", event);
                    try {
                        outputStream.close();
                    } catch (IOException ignore) {
                    }
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
