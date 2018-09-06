package x.tools.framework;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiStatus;
import x.tools.framework.error.BuilderError;
import x.tools.framework.error.InitializeError;
import x.tools.framework.error.XError;
import x.tools.framework.event.Event;
import x.tools.framework.event.EventBus;
import x.tools.framework.event.GlobalEventBus;
import x.tools.framework.event.annotation.AllEventSubscriber;
import x.tools.framework.event.json.IJsonSerializer;
import x.tools.framework.log.DefaultLoggerFactory;
import x.tools.framework.log.ILoggerFactory;
import x.tools.framework.log.LogApi;
import x.tools.framework.log.Loggable;
import x.tools.framework.script.IScriptEngine;

import static android.text.TextUtils.isEmpty;
import static x.tools.framework.XUtils.getProcessName;

public final class XContext extends ContextWrapper implements Loggable, EventBus {
    private static ILoggerFactory loggerFactory = new DefaultLoggerFactory();

    public static ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public static void setLoggerFactory(ILoggerFactory loggerFactory) {
        XContext.loggerFactory = loggerFactory;
    }

    public static class Builder {
        private Context context;
        private final Map<String, AbstractApi> apiMap = new HashMap<>();
        private IScriptEngine script = null;
        private String pathScript;
        private String pathData;
        private String pathTemp;
        private String scriptLooperName;
        private IJsonSerializer jsonSerializer;
        private String eventBusServerProcessName;

        public Builder(Context context) throws XError {
            this.context = context;
            File rootDir = context.getFilesDir();
            this.pathScript = new File(rootDir, "xScript").getAbsolutePath();
            this.pathData = new File(rootDir, "xData").getAbsolutePath();
            this.pathTemp = new File(context.getCacheDir(), "xTemp").getAbsolutePath();
            this.scriptLooperName = "Script-Looper";
            this.eventBusServerProcessName = context.getPackageName();

            // default api
            api(new XApi());
            api(new LogApi());
        }

        public Builder api(AbstractApi api) throws XError {
            if (apiMap.containsKey(api.getNamespace())) {
                throw new BuilderError(context.getString(R.string.X_TOOLS_CONFLICT_NAMESPACE, api.getNamespace()));
            }
            apiMap.put(api.getNamespace(), api);
            return this;
        }

        public Builder script(IScriptEngine script) {
            this.script = script;
            return this;
        }

        public Builder pathScript(String pathScript) {
            this.pathScript = pathScript;
            return this;
        }

        public Builder pathTemp(String pathTemp) {
            this.pathTemp = pathTemp;
            return this;
        }

        public Builder pathData(String pathData) {
            this.pathData = pathData;
            return this;
        }

        public Builder jsonSerializer(IJsonSerializer jsonSerializer) {
            this.jsonSerializer = jsonSerializer;
            return this;
        }

        public Builder scriptLooperName(String scriptLooperName) {
            this.scriptLooperName = scriptLooperName;
            return this;
        }

        public Builder eventBusServerProcessName(String eventBusServerProcessName) {
            this.eventBusServerProcessName = eventBusServerProcessName;
            return this;
        }

        public XContext build() {
            return new XContext(this);
        }
    }

    private final Map<String, AbstractApi> apiMap;
    private final IScriptEngine script;
    private final String pathScript;
    private final String pathData;
    private final String pathTemp;
    private String initError = null;
    private final String scriptLooperName;
    private Looper scriptLooper;
    private Handler scriptHandler;
    private IJsonSerializer jsonSerializer;
    private String eventBusServerProcessName;

    private XContext(Builder builder) {
        super(builder.context);
        this.apiMap = builder.apiMap;
        this.script = builder.script;
        this.pathScript = builder.pathScript;
        this.pathData = builder.pathData;
        this.pathTemp = builder.pathTemp;
        this.scriptLooperName = builder.scriptLooperName;
        this.jsonSerializer = builder.jsonSerializer;
        this.eventBusServerProcessName = builder.eventBusServerProcessName;
    }

    private void initScriptLooper() throws XError {
        SynchronousQueue<Looper> waitQueue = new SynchronousQueue<>();
        HandlerThread handlerThread = new HandlerThread(scriptLooperName) {
            @Override
            protected void onLooperPrepared() {
                try {
                    waitQueue.put(getLooper());
                } catch (InterruptedException ignore) {
                }
            }
        };
        handlerThread.setDaemon(false);
        handlerThread.start();
        try {
            scriptLooper = waitQueue.take();
            scriptHandler = new Handler(scriptLooper);
        } catch (InterruptedException e) {
            throw new InitializeError(e);
        }
    }

    private String getEventBusAddress() {
        return "EventBus-" + eventBusServerProcessName;
    }

    private boolean isEventBusServerProcess() {
        return eventBusServerProcessName.equals(getProcessName());
    }

    private boolean isInited = false;

    public void initialize() throws XError {
        if (isInited) return;

        // ensure data dir created
        File filePathData = new File(pathData);
        if (!filePathData.exists() || !filePathData.isDirectory()) {
            if (!filePathData.mkdirs()) {
                throw new InitializeError(getString(
                        R.string.X_TOOLS_CREATE_DIRECTORY_FAILED,
                        filePathData.getAbsolutePath()
                ));
            }
        }

        // ensure script dir created
        File filePathScript = new File(pathScript);
        if (!filePathScript.exists() || !filePathScript.isDirectory()) {
            if (!filePathScript.mkdirs()) {
                throw new InitializeError(getString(
                        R.string.X_TOOLS_CREATE_DIRECTORY_FAILED,
                        filePathScript.getAbsolutePath()
                ));
            }
        }

        // init event
        if (jsonSerializer != null) {
            GlobalEventBus.setJsonSerializer(jsonSerializer);
        } else {
            GlobalEventBus.setClassLoader(this.getClassLoader());
        }

        try {
            GlobalEventBus.getJsonSerializer();
            GlobalEventBus.initClient(getEventBusAddress());
            if (isEventBusServerProcess()) {
                GlobalEventBus.initServer(getEventBusAddress());
            }
            GlobalEventBus.subscribe(this);
        } catch (Throwable t) {
            initError = t.toString();
            throw new InitializeError(t);
        }


        // init script
        if (this.script != null) {
            initScriptLooper();
            initError = getString(R.string.X_TOOLS_INIT_SCRIPT_FAILED);
            this.script.init(this);
        }

        // init api
        List<AbstractApi> allApi = new ArrayList<>(this.apiMap.values());
        for (AbstractApi api : allApi) {
            initError = getString(
                    R.string.X_TOOLS_INIT_API_FAILED,
                    api.getClass(),
                    api.getNamespace()
            );
            if (!api.initialize(this)) {
                throw new InitializeError(initError);
            }
            String[] failedDependence = api.checkDependence(allApi);
            if (failedDependence != null) {
                initError = getString(
                        R.string.X_TOOLS_INIT_API_DEPENDENCE_FAILED,
                        api.getClass(),
                        api.getNamespace(),
                        Arrays.toString(failedDependence)
                );
                throw new InitializeError(initError);
            }
            if (this.script != null) {
                this.script.registerApi(api);
            }
        }
        initError = null;
        isInited = true;
    }

    public void runInScriptLooper(Runnable runnable) {
        scriptHandler.post(runnable);
    }

    public void runInScriptLooperDelayed(Runnable runnable, long delayMillis) {
        scriptHandler.postDelayed(runnable, delayMillis);
    }

    public String getPathScript() {
        return new File(pathScript).getAbsolutePath();
    }

    public String getPathTemp() {
        return new File(pathTemp).getAbsolutePath();
    }

    public String getPathData() {
        return new File(pathData).getAbsolutePath();
    }

    public String getPathScript(String subPath) {
        return new File(pathScript, subPath).getAbsolutePath();
    }

    public String getPathTemp(String subPath) {
        return new File(pathTemp, subPath).getAbsolutePath();
    }

    public String getPathData(String subPath) {
        return new File(pathData, subPath).getAbsolutePath();
    }

    public AbstractApi[] getAllApi() {
        Collection<AbstractApi> allApi = apiMap.values();
        return allApi.toArray(new AbstractApi[allApi.size()]);
    }

    public AbstractApi getApi(String namespace) {
        if (isEmpty(namespace))
            return null;

        for (AbstractApi api : this.apiMap.values()) {
            if (namespace.equals(api)) {
                return api;
            }
        }
        return null;
    }

    public <T extends AbstractApi> T getApi(Class<T> cls) {
        if (cls == null)
            return null;

        for (AbstractApi api : this.apiMap.values()) {
            if (ClassUtils.isAssignable(api.getClass(), cls)) {
                return (T) api;
            }
        }
        return null;
    }

    public AbstractApi[] getNotOkApi() {
        return null;
    }

    public XStatus checkStatus() {
        if (!isInited) {
            if (initError != null) {
                return XStatus.INIT_FAIL;
            }
            return XStatus.NOT_INIT;
        }

        for (AbstractApi api : this.apiMap.values()) {
            ApiStatus status = api.checkStatus();
            switch (status) {
                case OK:
                    break;
                default:
                    return XStatus.API_NOT_OK;
            }
        }
        return XStatus.OK;
    }

    public String statusDescription() {
        if (XStatus.OK.equals(checkStatus())) {
            return getString(R.string.X_TOOLS_OK);
        }

        StringBuilder sb = new StringBuilder();
        if (!isEmpty(initError)) {
            sb.append(initError).append("\n");
        }

        for (AbstractApi api : this.apiMap.values()) {
            ApiStatus status = api.checkStatus();
            if (ApiStatus.OK.equals(status)) {
                continue;
            }
            sb.append(getString(
                    R.string.X_TOOLS_API_STATUS_DESCRIPTION,
                    api.getClass().getName(),
                    api.getNamespace(),
                    api.statusDescription()
            )).append("\n");
        }
        return sb.toString();
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private boolean copyAssets(String from, String to) throws IOException {
        AssetManager assetManager = getAssets();
        String[] files = null;
        files = assetManager.list(from);
        if (files == null || files.length <= 0) {
            return false;
        }
        for (String filename : files) {
            String fullPath = from + File.separator + filename;
            String fullToPath = to + File.separator + filename;
            if (copyAssets(fullPath, fullToPath)) {
                continue;
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(fullPath);
                File outFile = new File(to, filename);
                if (outFile.exists()) {
                    outFile.delete();
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.exists() || !parent.isDirectory()) {
                        parent.mkdirs();
                    }
                }
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                throw e;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
        return true;
    }

    public boolean copyAssetsToDataDir(String path) throws IOException {
        return copyAssets(path, getPathData());
    }

    public boolean copyAssetsToScriptDir(String path) throws IOException {
        return copyAssets(path, getPathScript());
    }


    public static <T> T fromJson(String json, Class<T> cls) {
        return GlobalEventBus.fromJson(json, cls);
    }

    public static String toJson(Object object) {
        return GlobalEventBus.toJson(object);
    }

    @AllEventSubscriber
    public void onAllEvent(Event event) throws JSONException, XError {
        if (this.script != null) {
            this.script.dispatchEvent(event.getName(), event.getData());
        }
    }

    @Override
    public void subscribe(Object subscriber) {
        GlobalEventBus.subscribe(subscriber);
    }

    @Override
    public void unsubscribe(Object subscriber) {
        GlobalEventBus.unsubscribe(subscriber);
    }

    @Override
    public void trigger(String name) {
        GlobalEventBus.trigger(name);
    }

    @Override
    public void trigger(String name, Object data) {
        GlobalEventBus.trigger(name, data);
    }

    @Override
    public void triggerRaw(String name, JSONObject data) {
        GlobalEventBus.triggerRaw(name, data);
    }
}
