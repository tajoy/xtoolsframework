package x.tools.framework;

import android.content.Context;
import android.content.ContextWrapper;

import org.apache.commons.lang3.ClassUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiStatus;
import x.tools.framework.error.InitializeError;
import x.tools.framework.error.BuilderError;
import x.tools.framework.error.XError;
import x.tools.framework.log.DefaultLoggerFactory;
import x.tools.framework.log.ILoggerFactory;
import x.tools.framework.log.LogApi;
import x.tools.framework.log.Loggable;
import x.tools.framework.script.IScriptEngine;

import static android.text.TextUtils.isEmpty;

public final class XContext extends ContextWrapper implements Loggable {
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
//        private String scriptLooperName;

        public Builder(Context context) throws XError {
            this.context = context;
            File rootDir = context.getFilesDir();
            this.pathScript = new File(rootDir, "xScript").getAbsolutePath();
            this.pathData = new File(rootDir, "xData").getAbsolutePath();
            this.pathTemp =  new File(context.getCacheDir(), "xTemp").getAbsolutePath();
//            this.scriptLooperName = "Script-Looper";

            // default api
            api(new XApi());
            api(new LogApi());
        }

        public Builder api(AbstractApi api) throws XError {
            if (apiMap.containsKey(api.getNamespace())) {
                throw new BuilderError(context.getString(R.string.CONFLICT_NAMESPACE, api.getNamespace()));
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

//        public Builder scriptLooperName(String scriptLooperName) {
//            this.scriptLooperName = scriptLooperName;
//            return this;
//        }

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
//    private final String scriptLooperName;
//    private Looper scriptLooper;

    private XContext(Builder builder) {
        super(builder.context);
        this.apiMap = builder.apiMap;
        this.script = builder.script;
        this.pathScript = builder.pathScript;
        this.pathData = builder.pathData;
        this.pathTemp = builder.pathTemp;
//        this.scriptLooperName = builder.scriptLooperName;
    }

//    private void initScriptLooper() throws XError {
//        SynchronousQueue<Looper> waitQueue = new SynchronousQueue<>();
//        HandlerThread handlerThread = new HandlerThread(scriptLooperName) {
//            @Override
//            protected void onLooperPrepared() {
//                try {
//                    waitQueue.put(getLooper());
//                } catch (InterruptedException ignore) {
//                }
//            }
//        };
//        handlerThread.setDaemon(false);
//        handlerThread.start();
//        try {
//            scriptLooper = waitQueue.take();
//        } catch (InterruptedException e) {
//            throw new InitializeError(e);
//        }
//    }


    private boolean isInited = false;

    public void initialize() throws XError {
        if (isInited) return;

        File filePathData = new File(pathData);
        if (!filePathData.isDirectory()) {
            if (!filePathData.mkdirs()) {
                throw new InitializeError(getString(
                        R.string.CREATE_DIRECTORY_FAILED,
                        filePathData.getAbsolutePath()
                ));
            }
        }

        File filePathScript = new File(pathScript);
        if (!filePathScript.isDirectory()) {
            if (!filePathScript.mkdirs()) {
                throw new InitializeError(getString(
                        R.string.CREATE_DIRECTORY_FAILED,
                        filePathScript.getAbsolutePath()
                ));
            }
        }


//        initScriptLooper();
        if (this.script != null) {
            initError = getString(R.string.INIT_SCRIPT_FAILED);
            this.script.init(this);
        }
        for (AbstractApi api : this.apiMap.values()) {
            initError = getString(
                    R.string.INIT_API_FAILED,
                    api.getClass(),
                    api.getNamespace()
            );
            if (!api.initialize(this)) {
                throw new InitializeError(initError);
            }
            if (this.script != null) {
                this.script.registerApi(api);
            }
        }
        initError = null;
        isInited = true;
    }

//    public void runInScriptLooper(Runnable runnable) {
//    }

    public String getPathScript() {
        return pathScript;
    }

    public String getPathTemp() {
        return pathTemp;
    }

    public String getPathData() {
        return pathData;
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

    public String statusDescreption() {
        if (XStatus.OK.equals(checkStatus())) {
            return getString(R.string.OK);
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
                    R.string.API_STATUS_DESCRIPTION,
                    api.getClass().getName(),
                    api.getNamespace(),
                    api.statusDescription()
            )).append("\n");
        }
        return sb.toString();
    }


}
