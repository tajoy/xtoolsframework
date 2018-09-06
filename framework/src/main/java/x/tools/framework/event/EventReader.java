package x.tools.framework.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class EventReader {

    private final Lock lock = new ReentrantLock();
    private Condition notNull = lock.newCondition();
    private final AtomicReference<InputStream> inputStream = new AtomicReference<>(null);


    EventReader() {
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream.set(inputStream);
        if (!lock.tryLock())
            notNull.notifyAll();
    }

    public Event readEvent() {
        Event event = null;

        try {
            do {
                InputStream inputStream;
                lock.lock();
                try {
                    inputStream = this.inputStream.get();
                    while (inputStream == null) {
                        notNull.await();
                        inputStream = this.inputStream.get();
                    }
                } finally {
                    try {
                        lock.unlock();
                    } catch (Throwable ignore) {
                    }
                }

                try {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    event = (Event) ois.readObject();
                } catch (NullPointerException | IOException e) {
                    Thread.sleep(1000);
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
