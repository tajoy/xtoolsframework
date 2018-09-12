package x.tools.eventbus.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import x.tools.eventbus.EventBus;

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
    public static <T extends I, I> void registerProxyHost(Class<I> iface, T proxyHost) {
        RpcProxyHost<T, I> host = new RpcProxyHost<>(iface, proxyHost);
        if (proxyHostMap.containsKey(host.getKey())) {
            return;
        }
        proxyHostMap.put(host.getKey(), host);
        EventBus.addInterpolator(host);
    }


    /**
     * 获取/创建远程调用代理, 通过 interface 定义调用接口, 通过 id 指定调用目标
     *
     * @param iface 调用接口定义的接口类
     * @param id 调用目标的 id, 一般是进程名
     * @param <I> 调用接口定义的接口类型
     * @return 创建的代理接口或者本地对象
     */
    public static <I> I getProxy(Class<I> iface, String id) {
        if (EventBus.getId().equals(id)) {
            for (RpcProxyHost<?, ?> host: proxyHostMap.values()) {
                if (host.iface.equals(iface)) {
                    return (I) host.host;
                }
            }
            return null;
        }

        RpcProxy<I> proxy = new RpcProxy<>(iface, id);
        if (proxyMap.containsKey(proxy.getKey())) {
            return (I) proxyMap.get(proxy.getKey()).getProxy();
        }
        proxyMap.put(proxy.getKey(), proxy);
        EventBus.addInterpolator(proxy);
        return proxy.getProxy();
    }

}
