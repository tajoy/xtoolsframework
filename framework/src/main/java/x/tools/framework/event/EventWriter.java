package x.tools.framework.event;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventWriter {
    private final Lock lock = new ReentrantLock();
    private Condition notNull = lock.newCondition();
    private final AtomicReference<OutputStream> outputStream = new AtomicReference<>(null);

    EventWriter() {
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream.set(outputStream);
        if (!lock.tryLock())
            notNull.notifyAll();
    }

    public void writeEvent(Event event) {
        try {
            do {
                OutputStream outputStream;
                lock.lock();
                try {
                    outputStream = this.outputStream.get();
                    while (outputStream == null) {
                        notNull.await();
                        outputStream = this.outputStream.get();
                    }
                } finally {
                    try {
                        lock.unlock();
                    } catch (Throwable ignore) {
                    }
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
