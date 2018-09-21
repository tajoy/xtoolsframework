package x.tools.framework.script.lua.lib;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LuaTimerLib extends TwoArgFunction {
    private final Timer timer = new Timer("Lua-Timer", true);
    private final LuaTable module = new LuaTable();
    private final Map<String, LuaTask> timerMap = new ConcurrentHashMap<>();

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        String namespace = "timer";
        module.set("schedule", new schedule());
        module.set("scheduleOnce", new scheduleOnce());
        module.set("cancel", new cancel());
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return env;
    }


    private final class LuaTask extends TimerTask {
        private final String id;
        private final LuaValue func;

        LuaTask(String id, LuaValue func) {
            this.id = id;
            this.func = func;
        }

        @Override
        public void run() {
            func.invoke(valueOf(id));
        }
    }

    private final class schedule extends ThreeArgFunction {
        @Override
        public LuaValue call(LuaValue func, LuaValue arg2, LuaValue arg3) {
            long delay = arg2.checklong();
            long period = arg3.checklong();
            String id = UUID.randomUUID().toString();
            LuaTask task = new LuaTask(id, func);
            timer.schedule(task, delay, period);
            timerMap.put(id, task);
            return LuaValue.valueOf(id);
        }
    }

    private final class scheduleOnce extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue func, LuaValue arg2) {
            long delay = arg2.checklong();
            String id = UUID.randomUUID().toString();
            LuaTask task = new LuaTask(id, func);
            timer.schedule(task, delay);
            timerMap.put(id, task);
            return LuaValue.valueOf(id);
        }
    }

    private final class cancel extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            String id = arg.checkjstring();
            if (id == null) {
                return FALSE;
            }
            LuaTask task = timerMap.get(id);
            if (task == null) {
                return FALSE;
            }
            return valueOf(task.cancel());
        }
    }
}
