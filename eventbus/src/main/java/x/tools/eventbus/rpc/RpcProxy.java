package x.tools.eventbus.rpc;

import java.util.Objects;

import x.tools.eventbus.Event;
import x.tools.eventbus.IEventInterpolator;

class RpcProxy<I> implements IEventInterpolator {
    public final Class<I> iface;
    public final String id;

    public RpcProxy(Class<I> iface, String id) {
        this.iface = iface;
        this.id = id;
    }

    private String key;
    public String getKey() {
        if (key == null) {
            key = iface.getName() + "-" + id;
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RpcProxy)) return false;
        RpcProxy<?> proxyInfo = (RpcProxy<?>) o;
        return Objects.equals(id, proxyInfo.id) &&
                Objects.equals(iface, proxyInfo.iface);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, iface);
    }

    public I getProxy() {
        return null;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
