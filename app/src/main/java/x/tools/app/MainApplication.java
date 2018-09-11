package x.tools.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import x.tools.framework.XContext;
import x.tools.framework.XUtils;
import x.tools.framework.api.image.ImageApi;
import x.tools.framework.api.screencap.ScreencapApi;
import x.tools.framework.script.lua.LuaScript;

public class MainApplication extends Application {
    private static XContext xContext;
    private static final LuaScript luaScript = new LuaScript();

    private final Gson gson = new Gson(); // just for load class

    public static XContext getXContext() {
        return xContext;
    }

    private synchronized static void initXContext(Application application) {
        if (xContext == null) {
            try {
                xContext = new XContext.Builder(application)
                        .script(luaScript)
                        .api(ScreencapApi.getInstance(application))
                        .api(new ImageApi())
                        .eventBusServerProcessName(BuildConfig.APPLICATION_ID + ":main_service")
                        .build();
                xContext.copyAssetsToScriptDir("script");
                xContext.initialize();
                xContext.subscribe(Status.getInst());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initXContext(this);
        MainService.bootService(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
