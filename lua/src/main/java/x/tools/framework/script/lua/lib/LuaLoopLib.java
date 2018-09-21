package x.tools.framework.script.lua.lib;

import android.os.Handler;
import android.os.Looper;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class LuaLoopLib extends TwoArgFunction {
    private LuaTable module = new LuaTable();
    private final Handler mainHandler;

    public LuaLoopLib(Looper mainLooper) {
        this.mainHandler = new Handler(mainLooper);
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        String namespace = "loop";
        module.set("runInMain", new runInMain());
        module.set("runInMainDelayed", new runInMainDelayed());
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return env;
    }

    private final class runInMain extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            mainHandler.post(arg::invoke);
            return NONE;
        }
    }

    private final class runInMainDelayed extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            long delay = arg2.checklong();
            mainHandler.postDelayed(arg1::invoke, delay);
            return NONE;
        }
    }

}
