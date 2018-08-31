package x.tools.framework;

import android.content.Context;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.InitializeError;
import x.tools.framework.error.XError;
import x.tools.framework.log.DefaultLoggerFactory;
import x.tools.framework.log.ILoggerFactory;
import x.tools.framework.log.LogApi;
import x.tools.framework.log.Loggable;
import x.tools.framework.script.IScriptEngine;

public final class XContext implements Loggable {
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

        public Builder(Context context) {
            this.context = context;
            File storageDir = Environment.getExternalStorageDirectory();
            this.pathScript = new File(storageDir, "xScript").getAbsolutePath();
            this.pathData = new File(storageDir, "xData").getAbsolutePath();
            this.pathTemp = context.getCacheDir().getAbsolutePath();
//            this.scriptLooperName = "Script-Looper";

            // default api
            api(new XApi());
            api(new LogApi());
        }

        public Builder api(AbstractApi api) {
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

    private Context context;
    private final Map<String, AbstractApi> apiMap;
    private final IScriptEngine script;
    private final String pathScript;
    private final String pathData;
    private final String pathTemp;
//    private final String scriptLooperName;
//    private Looper scriptLooper;

    private XContext(Builder builder) {
        this.context = builder.context;
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

//        initScriptLooper();
        this.script.init(this);
        for (AbstractApi api : this.apiMap.values()) {
            if (!api.initialize(this)) {
                throw new InitializeError(String.format(
                        "api %s[%s] initialize failed!",
                        api.getClass(),
                        api.getNamespace())
                );
            }
        }
        isInited = true;
    }

//    public void runInScriptLooper(Runnable runnable) {
//    }

    public Context getContext() {
        return context;
    }

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

    public InputStream findResource(String filename) {
        if (filename == null) return null;
        File f;
        do {
            f = new File(getPathTemp(filename));
            if (f.exists())
                break;

            f = new File(getPathData(filename));
            if (f.exists())
                break;

            f = new File(getPathScript(filename));
            if (f.exists())
                break;
        } while(Boolean.parseBoolean("false"));

        if (f.exists())
            return null;
        try {
            return new FileInputStream(f);
        } catch (IOException ioe) {
            return null;
        }
    }
}
