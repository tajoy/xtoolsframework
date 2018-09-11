package x.tools.framework.event;

import org.json.JSONObject;

import java.io.IOException;

import x.tools.framework.event.json.FastJsonSerializer;
import x.tools.framework.event.json.GsonSerializer;
import x.tools.framework.event.json.IJsonSerializer;

public class EventBus {

    private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private static IJsonSerializer jsonSerializer = null;
    private static EventBusServer server = null;
    private static EventBusClient client = null;
    private static boolean isServer = false;

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    public static void setClassLoader(ClassLoader classLoader) {
        EventBus.classLoader = classLoader;
    }

    public static synchronized IJsonSerializer getJsonSerializer() {
        if (jsonSerializer == null) {
            try {
                jsonSerializer = new GsonSerializer();
                return jsonSerializer;
            } catch (Throwable ignore) {
            }
            try {
                jsonSerializer = new FastJsonSerializer();
                return jsonSerializer;
            } catch (Throwable ignore) {
            }
        }
        if (jsonSerializer == null) throw new AssertionError("jsonSerializer == null");
        return jsonSerializer;
    }

    public static void setJsonSerializer(IJsonSerializer serializer) {
        jsonSerializer = serializer;
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        return getJsonSerializer().fromJson(json, cls);
    }

    public static String toJson(Object object) {
        return getJsonSerializer().toJson(object);
    }

    /**
     * 初始化事件总线服务器, 为事件总线提供IPC服务
     * @param address unix 本地套接字地址
     * @return 是否初始化成功
     * @throws IOException 监听异常
     */
    public static boolean initServer(String address) throws IOException {
        if (server != null)
            return true;
        server = new EventBusServer(address);
        isServer = true;
        return true;
    }

    /**
     * 初始化事件总线客户端, 为本地事件总线做准备
     * address 不为 null 的话并一直尝试连接到事件总线服务器
     * 同时处理本地事件和IPC事件
     *
     * @param address unix 本地套接字地址, 可为 null
     * @return 是否初始化成功
     */
    public static boolean initClient(String address) {
        if (client != null)
            return true;
        client = new EventBusClient(address);
        return true;
    }

    public static IEventBus getEventBus() {
        return client;
    }

    public static void addListener(IEventListener listener) {
        if (client == null) throw new AssertionError("client == null");
        client.addListener(listener);
    }

    public static void removeListener(IEventListener listener) {
        if (client == null) throw new AssertionError("client == null");
        client.removeListener(listener);
    }

    public static void subscribe(Object subscriber) {
        if (client == null) throw new AssertionError("client == null");
        client.subscribe(subscriber);
    }

    public static void unsubscribe(Object subscriber) {
        if (client == null) throw new AssertionError("client == null");
        client.unsubscribe(subscriber);
    }

    public static void trigger(String name) {
        if (client == null) throw new AssertionError("client == null");
        client.trigger(name);
    }

    public static void trigger(String name, Object data) {
        if (client == null) throw new AssertionError("client == null");
        client.trigger(name, data);
    }

    public static void triggerRaw(String name, JSONObject data) {
        if (client == null) throw new AssertionError("client == null");
        client.triggerRaw(name, data);
    }

    public static boolean isServer() {
        return isServer;
    }

    public static String getId() {
        if (client == null) throw new AssertionError("client == null");
        return client.getId();
    }
}
