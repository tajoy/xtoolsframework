package x.tools.eventbus.rpc;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import x.tools.eventbus.Event;
import x.tools.eventbus.EventBus;
import x.tools.eventbus.IEventInterpolator;
import x.tools.eventbus.log.Loggable;


public class RpcInvocationHandler<I> implements InvocationHandler, Loggable, IEventInterpolator {
    private final RpcProxy<I> rpcProxy;
    private final Map<String, RpcContext> contexts = new ConcurrentHashMap<>();

    public RpcInvocationHandler(RpcProxy<I> rpcProxy) {
        this.rpcProxy = rpcProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcContext context = new RpcContext(method.getReturnType());
        contexts.put(context.getUUID(), context);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RpcConstants.KEY_TARGET, rpcProxy.process + "#" + rpcProxy.id);
        jsonObject.put(RpcConstants.KEY_IFACE, rpcProxy.iface.getName());
        jsonObject.put(RpcConstants.KEY_METHOD, method.getName());
        jsonObject.put(RpcConstants.KEY_ID, context.getUUID());

        JSONArray parameterTypesArray = new JSONArray();
        Class[] parameterTypes = method.getParameterTypes();
        for (Class type: parameterTypes) {
            parameterTypesArray.put(type.getName());
        }
        jsonObject.put(RpcConstants.KEY_PARAMETER_TYPES, parameterTypesArray);
        JSONArray parameterArray = new JSONArray();
        for (int i = 0; args != null && i < args.length; i++) {
            Class argType = parameterTypes[i];
            Object arg = args[i];
            if (arg == null) {
                parameterArray.put(JSONObject.NULL);
            } else if (ClassUtils.isPrimitiveOrWrapper(argType) || ClassUtils.isAssignable(argType, String.class)) {
                parameterArray.put(arg);
            } else if (arg instanceof Collection || argType.isArray()) {
                parameterArray.put(new JSONArray(EventBus.toJson(arg)));
            } else {
                parameterArray.put(new JSONObject(EventBus.toJson(arg)));
            }
        }
        jsonObject.put(RpcConstants.KEY_PARAMETERS, parameterArray);
        debug("trigger event EVENT_NAME_CALL: %s", jsonObject.toString(2));
        EventBus.triggerRaw(RpcConstants.EVENT_NAME_CALL, jsonObject);
        if (!context.await()) throw new RpcInvocationTimeoutException();
        if (context.getThrowable() != null) {
            throw context.getThrowable();
        }
        return context.getData();
    }

    @Override
    public boolean onEvent(Event event) {
        if (!event.getName().equals(RpcConstants.EVENT_NAME_RETURN)) return false;

        JSONObject jsonObject;
        try {
            jsonObject = event.getData();
        } catch (JSONException e) {
            error(e, "unknown error");
            // 格式错误, 后续不处理
            return true;
        }

        try {
            debug("on event EVENT_NAME_RETURN: %s", jsonObject.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String id;
        try {
            id = jsonObject.getString(RpcConstants.KEY_ID);
        } catch (JSONException e) {
            error(e, "cannot get %s", RpcConstants.KEY_ID);
            return false;
        }
        RpcContext context = contexts.get(id);
        if (context == null) {
            return false;
        }
        contexts.remove(id);

        if (jsonObject.has(RpcConstants.KEY_THROWABLE)) {
            Throwable throwable;
            try {
                throwable = EventBus.fromJson(
                        jsonObject.getJSONObject(RpcConstants.KEY_THROWABLE).toString(),
                        Throwable.class
                );
            } catch (Exception e) {
                throwable = e;
            }
            context.setThrowable(throwable);
        } else {
                if (jsonObject.has(RpcConstants.KEY_RETURN)) {
                try {
                    Class<?> retType = context.getReturnType();
                    if (jsonObject.has(RpcConstants.KEY_RETURN_TYPE) && !jsonObject.isNull(RpcConstants.KEY_RETURN_TYPE)) {
                        retType = ClassUtils.getClass(jsonObject.getString(RpcConstants.KEY_RETURN_TYPE));
                    }
                    Object data;
                    Object object = jsonObject.get(RpcConstants.KEY_RETURN);
                    if (JSONObject.NULL.equals(object)) {
                        data = null;
                    } else if (ClassUtils.isPrimitiveOrWrapper(retType)) {
                        data = object;
                    } else if (object instanceof JSONObject || object instanceof JSONArray) {
                        data = EventBus.fromJson(
                                object.toString(),
                                retType
                        );
                    } else {
                        data = object;
                    }
                    context.setData(data);
                } catch (Exception e) {
                    context.setThrowable(e);
                }
            } else {
                context.setData(null);
            }
        }
        // 已处理
        return true;
    }
}
