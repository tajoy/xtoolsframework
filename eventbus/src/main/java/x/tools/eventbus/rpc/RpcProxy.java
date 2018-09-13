package x.tools.eventbus.rpc;

import java.lang.reflect.Proxy;
import java.util.Objects;

import x.tools.eventbus.Event;
import x.tools.eventbus.IEventInterpolator;

class RpcProxy<I> implements IEventInterpolator {
    public final Class<I> iface;
    public final String target;

    public RpcProxy(Class<I> iface, String target) {
        this.iface = iface;
        this.target = target;
    }

    private String key;

    public String getKey() {
        if (key == null) {
            key = iface.getName() + "-" + target;
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RpcProxy)) return false;
        RpcProxy<?> proxyInfo = (RpcProxy<?>) o;
        return Objects.equals(target, proxyInfo.target) &&
                Objects.equals(iface, proxyInfo.iface);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, iface);
    }

    private RpcInvocationHandler invocationHandler;
    private I proxy;

    public synchronized I getProxy() {
        if (proxy == null) {
            invocationHandler = new RpcInvocationHandler(this);
            proxy = (I) Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class<?>[]{iface},
                    invocationHandler
            );
        }
        return proxy;
    }

    @Override
    public boolean onEvent(Event event) {
        if (invocationHandler != null) {
            return invocationHandler.onEvent(event);
        }
        return false;
    }
}
