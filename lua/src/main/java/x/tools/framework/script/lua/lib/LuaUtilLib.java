package x.tools.framework.script.lua.lib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import x.tools.eventbus.EventBus;

public class LuaUtilLib extends TwoArgFunction {
    private LuaTable module = new LuaTable();

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        String namespace = "util";
        module.set("getTid", new getTid());
        module.set("getPid", new getPid());
        module.set("getProcessName", new getProcessName());
        module.set("getThreadName", new getThreadName());
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return env;
    }

    private final class getTid extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(Thread.currentThread().getId());
        }
    }

    private final class getPid extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(android.os.Process.myPid());
        }
    }

    private final class getThreadName extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(Thread.currentThread().getName());
        }
    }

    private final class getProcessName extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return LuaValue.valueOf(EventBus.getProcessName());
        }
    }
}
