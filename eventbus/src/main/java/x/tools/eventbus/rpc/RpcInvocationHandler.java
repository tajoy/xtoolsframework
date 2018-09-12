package x.tools.eventbus.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import x.tools.eventbus.Event;
import x.tools.eventbus.IEventInterpolator;
import x.tools.eventbus.log.Loggable;

public class RpcInvocationHandler implements InvocationHandler, Loggable, IEventInterpolator {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
