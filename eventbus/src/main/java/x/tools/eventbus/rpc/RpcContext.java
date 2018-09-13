package x.tools.eventbus.rpc;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcContext {
    private final Class<?> returnType;
    private final String uuid = UUID.randomUUID().toString();
    private CountDownLatch latch = new CountDownLatch(1);
    private Object data;
    private Throwable throwable;

    public RpcContext(Class returnType) {
        this.returnType = returnType;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public String getUUID() {
        return uuid;
    }

    boolean await() throws InterruptedException {
        return latch.await(30, TimeUnit.SECONDS);
    }

    void setData(Object data) {
        this.data = data;
        latch.countDown();
    }

    Object getData() {
        return data;
    }

    void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        latch.countDown();
    }

    Throwable getThrowable() {
        return throwable;
    }
}
