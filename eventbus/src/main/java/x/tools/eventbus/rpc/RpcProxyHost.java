package x.tools.eventbus.rpc;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import x.tools.eventbus.Event;
import x.tools.eventbus.EventBus;
import x.tools.eventbus.IEventInterpolator;
import x.tools.eventbus.log.Loggable;

class RpcProxyHost<T extends I, I> implements Loggable, IEventInterpolator {
    public final Class<I> iface;
    public final T host;

    public RpcProxyHost(Class<I> iface, T host) {
        this.iface = iface;
        this.host = host;
    }

    private String key;

    public String getKey() {
        if (key == null) {
            key = iface.getName();
        }
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RpcProxyHost)) return false;
        RpcProxyHost<?, ?> that = (RpcProxyHost<?, ?>) o;
        return Objects.equals(iface, that.iface) &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iface, host);
    }

    @Override
    public boolean onEvent(Event event) {
        if (!event.getName().equals(RpcConstants.EVENT_NAME_CALL)) return false;

        JSONObject jsonObject;
        try {
            jsonObject = event.getData();
        } catch (JSONException e) {
            error(e, "unknown error");
            // 格式错误, 后续不处理
            return true;
        }
        String target;
        try {
            target = jsonObject.getString(RpcConstants.KEY_TARGET);
        } catch (JSONException e) {
            error(e, "cannot get %s", RpcConstants.KEY_TARGET);
            // 格式错误, 后续不处理
            return true;
        }
        if (!EventBus.getId().equals(target)) {
            // 不是本进程的, 后续不处理
            return true;
        }
        String iface = null;
        try {
            iface = jsonObject.getString(RpcConstants.KEY_IFACE);
        } catch (JSONException e) {
            error(e, "cannot get %s", RpcConstants.KEY_IFACE);
            return false;
        }
        if (!this.iface.getName().equals(iface)) {
            // 有可能是本进程其他的, 后续继续处理
            return false;
        }

        String id = null;
        try {
            id = jsonObject.getString(RpcConstants.KEY_ID);
        } catch (JSONException e) {
            error(e, "cannot get %s", RpcConstants.KEY_ID);
            return false;
        }

        try {
            String methodName = jsonObject.getString(RpcConstants.KEY_METHOD);
            JSONArray parameterTypesArray = jsonObject.getJSONArray(RpcConstants.KEY_PARAMETER_TYPES);
            JSONArray parameterArray = jsonObject.getJSONArray(RpcConstants.KEY_PARAMETERS);
            if (parameterTypesArray.length() != parameterArray.length()) {
                error("parameterTypesArray.length() != parameterArray.length(): %s", event);
                // 本应这里处理, 但是出错, 后续不处理
                return true;
            }
            int parameterCount = parameterTypesArray.length();

            Class<?>[] parameterTypes = new Class[parameterCount];
            ClassLoader cl = this.iface.getClassLoader();
            for (int i = 0; i < parameterCount; i++) {
                String typeName = parameterTypesArray.getString(i);
                try {
                    parameterTypes[i] = cl.loadClass(typeName);
                } catch (ClassNotFoundException e) {
                    error( e, "cannot load class: %s", typeName);
                    // 载入类型出错, 后续不处理
                    return true;
                }
            }
            Method method;
            try {
                // test try get from iface
                this.iface.getDeclaredMethod(methodName, parameterTypes);
                method = this.host.getClass().getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                error( e, "cannot find method: %s, %s", methodName, Arrays.toString(parameterTypes));
                // 找方法出错, 后续不处理
                return true;
            }
            Object[] parameters = new Class[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                Class type = parameterTypes[i];
                if (ClassUtils.isAssignable(boolean.class, type)) {
                    parameters[i] = parameterArray.getBoolean(i);
                    continue;
                }
                if (ClassUtils.isAssignable(int.class, type)) {
                    parameters[i] = parameterArray.getInt(i);
                    continue;
                }
                if (ClassUtils.isAssignable(long.class, type)) {
                    parameters[i] = parameterArray.getLong(i);
                    continue;
                }
                if (ClassUtils.isAssignable(double.class, type)) {
                    parameters[i] = parameterArray.getDouble(i);
                    continue;
                }
                if (ClassUtils.isAssignable(String.class, type)) {
                    parameters[i] = parameterArray.getString(i);
                    continue;
                }
                Object parm = parameterArray.get(i);
                if (parm instanceof JSONObject || parm instanceof JSONArray) {
                    parameters[i] = EventBus.fromJson(parm.toString(), type);
                } else {
                    parameters[i] = parm;
                }
            }

            try {
                Object ret = method.invoke(host, parameters);

            } catch (Throwable t) {
            }
        } catch (JSONException e) {
            error(e, "json format error");
        }
        return true;
    }
}
