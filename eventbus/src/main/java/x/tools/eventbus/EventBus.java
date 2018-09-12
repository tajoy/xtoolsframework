package x.tools.eventbus;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import x.tools.eventbus.annotation.ThreadMode;
import x.tools.eventbus.json.FastJsonSerializer;
import x.tools.eventbus.json.GsonSerializer;
import x.tools.eventbus.json.IJsonSerializer;
import x.tools.eventbus.log.DefaultLoggerFactory;
import x.tools.eventbus.log.ILoggerFactory;
import x.tools.eventbus.log.Loggable;

public class EventBus implements Loggable {
    private EventBus() {
    }

    private static EventBus INSTANCE = new EventBus();

    private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    private static IJsonSerializer jsonSerializer = null;
    private static EventBusServer server = null;
    private static EventBusClient client = null;
    private static Context context = null;
    private static Handler mainHandler = null;
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
     *
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

    private static final Map<String, Looper> looperMap = new HashMap<>();
    private static final Map<String, Handler> handlerMap = new HashMap<>();

    private static Looper getLooper(String looperName) {
        Looper looper;
        synchronized (looperMap) {
            looper = looperMap.get(looperName);
        }

        if (looper == null) {
            if ("MAIN".equalsIgnoreCase(looperName)) {
                looper = Looper.getMainLooper();
            } else {
                HandlerThread handlerThread = new HandlerThread("LOOPER-" + looperName);
                handlerThread.setDaemon(false);
                handlerThread.start();
                while (true) {
                    try {
                        looper = handlerThread.getLooper();
                        if (looper == null) {
                            try {
                                INSTANCE.debug("getLooper getLooper is null, wait 100 and retry");
                                Thread.sleep(100);
                            } catch (Throwable t) {
                            }
                            continue;
                        }
                        break;
                    } catch (Throwable t) {
                        try {
                            INSTANCE.debug(t, "getLooper getLooper error, wait 100 and retry");
                            Thread.sleep(100);
                        } catch (Throwable t2) {
                        }
                        continue;
                    }
                }
            }
            synchronized (looperMap) {
                looperMap.put(looperName, looper);
            }
            synchronized (handlerMap) {
                handlerMap.remove(looperName);
            }
        }
        return looper;
    }

    private static Handler getLooperHandler(String looperName) {
        Handler handler;
        Looper looper = getLooper(looperName);

        synchronized (handlerMap) {
            handler = handlerMap.get(looperName);
        }

        if (handler == null) {
            handler = new Handler(looper);
            synchronized (handlerMap) {
                handlerMap.put(looperName, handler);
            }
        }

        return handler;
    }

    private static Handler getAsyncHandler(int index) {
        return getLooperHandler("EventBus-Async-Thread-" + index);
    }

    /**
     * 初始化事件总线客户端, 为本地事件总线做准备
     * address 不为 null 的话并一直尝试连接到事件总线服务器
     * 同时处理本地事件和IPC事件
     *
     * @param address unix 本地套接字地址, 可为 null
     * @return 是否初始化成功
     */
    public static boolean initClient(Context context, String address) {
        if (client != null)
            return true;
        EventBus.context = context;
        EventBus.client = new EventBusClient(address);
        EventBus.mainHandler = new Handler(context.getMainLooper());
        return true;
    }

    public static IEventBus getEventBus() {
        return client;
    }

    public static void addInterpolator(IEventInterpolator interpolator) {
        if (client == null) throw new AssertionError("client == null");
        client.addInterpolator(interpolator);
    }

    public static void removeInterpolator(IEventInterpolator interpolator) {
        if (client == null) throw new AssertionError("client == null");
        client.removeInterpolator(interpolator);
    }

    public static void subscribe(Object subscriber) {
        if (client == null) throw new AssertionError("client == null");
        client.subscribe(subscriber);
    }

    public static void unsubscribe(Object subscriber) {
        if (client == null) throw new AssertionError("client == null");
        client.unsubscribe(subscriber);
    }

    public static void trigger(Event event) {
        if (client == null) throw new AssertionError("client == null");
        client.trigger(event);
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

    private static final Random random = new Random();


    public static void callIn(ThreadMode mode, Runnable runnable) {
        switch (mode) {
            case POSTING:
                runnable.run();
                break;
            case ASYNC:
                Looper myLooper = Looper.myLooper();
                Handler asyncHandler_1 = getAsyncHandler(1);
                Handler asyncHandler_2 = getAsyncHandler(2);
                if (asyncHandler_1.getLooper().equals(myLooper)) {
                    asyncHandler_2.post(runnable);
                } else if (asyncHandler_2.getLooper().equals(myLooper)) {
                    asyncHandler_1.post(runnable);
                } else {
                    if (random.nextBoolean()) {
                        asyncHandler_1.post(runnable);
                    } else {
                        asyncHandler_2.post(runnable);
                    }
                }
                break;
            case MAIN:
                mainHandler.post(runnable);
                break;
        }
    }

    private static ILoggerFactory loggerFactory = new DefaultLoggerFactory();

    public static ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public static void setLoggerFactory(ILoggerFactory loggerFactory) {
        EventBus.loggerFactory = loggerFactory;
    }

    public static String getProcessName(int pid) {
        String processName;
        String pidStr = String.valueOf(pid);
        try {
            File file = new File("/proc/" + pidStr + "/cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            return null;
        }
    }

    private static String processName = null;

    public static String getProcessName() {
        if (processName == null) {
            int pid = android.os.Process.myPid();
            processName = getProcessName(pid);
        }
        return processName;
    }
}
