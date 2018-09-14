package x.tools.eventbus.rpc;

import android.app.Application;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import x.tools.eventbus.EventBus;

import static x.tools.eventbus.EventBus.getProcessName;


public class RpcFactory {
    private static final ConcurrentMap<String, RpcProxyHost> proxyHostMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, RpcProxy> proxyMap = new ConcurrentHashMap<>();

    /**
     * 注册远程调用处理宿主, 用于处理其他进程的远程调用请求
     *
     * @param iface 调用接口定义的接口类
     * @param proxyHost 处理远程调用处理宿主的实例对象
     * @param <T> 处理远程调用处理宿主类型
     * @param <I> 调用接口定义的接口类型
     */
    public static <T extends I, I> boolean registerProxyHost(Class<I> iface, T proxyHost, String id) {
        String key = RpcProxyHost.getKey(iface, id);
        if (proxyHostMap.containsKey(key)) {
            return false;
        }
        RpcProxyHost<T, I> host = new RpcProxyHost<>(iface, proxyHost, id);
        proxyHostMap.put(key, host);
        EventBus.addInterpolator(host);
        return true;
    }


    /**
     * 注销远程调用处理宿主, 用于处理其他进程的远程调用请求
     *
     * @param iface 调用接口定义的接口类
     * @param <T> 处理远程调用处理宿主类型
     * @param <I> 调用接口定义的接口类型
     */
    public static <T extends I, I> boolean unregisterProxyHost(Class<I> iface, T proxyHost, String id) {
        String key = RpcProxyHost.getKey(iface, id);
        if (proxyHostMap.containsKey(key)) {
            RpcProxyHost<T, I> host = proxyHostMap.get(key);
            if (proxyHost == host.host) {
                proxyHostMap.remove(key);
                EventBus.removeInterpolator(host);
                return true;
            }
        }
        return false;
    }


    /**
     * 获取/创建远程调用代理, 通过 interface 定义调用接口, 通过 id 指定调用目标
     *
     * @param iface 调用接口定义的接口类
     * @param process 调用目标进程名
     * @param id 调用目标的 id, 调用 {@link #registerProxyHost} 注册时指定的 id
     * @param <I> 调用接口定义的接口类型
     * @return 创建的代理接口或者本地对象
     */
    public static <I> I getProxy(Class<I> iface, String process, String id) {
        if (getProcessName().equals(process)) {
            for (RpcProxyHost<?, ?> host: proxyHostMap.values()) {
                if (host.iface.equals(iface)) {
                    return (I) host.host;
                }
            }
            return null;
        }

        String key = RpcProxy.getKey(iface, process, id);
        if (proxyMap.containsKey(key)) {
            return (I) proxyMap.get(key).getProxy();
        }
        RpcProxy<I> proxy = new RpcProxy<>(iface, process, id);
        proxyMap.put(key, proxy);
        EventBus.addInterpolator(proxy);
        return proxy.getProxy();
    }

}
