package x.tools.framework.script.lua.lib;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;

import x.tools.eventbus.EventBus;
import x.tools.framework.script.lua.LuaScript;
import x.tools.log.Loggable;

public class LuaUtilLib extends TwoArgFunction {
    private static final Loggable logger = Loggable.fromName("lua-util");
    private LuaTable module = new LuaTable();
    private Globals globals;

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        globals = env.checkglobals();
        String namespace = "util";
        module.set("getTid", new getTid());
        module.set("getPid", new getPid());
        module.set("getProcessName", new getProcessName());
        module.set("getThreadName", new getThreadName());
        module.set("printCallStack", new printCallStack());
        module.set("callMethodExact", new callMethodExact());
        module.set("toJSON", new toJSON());
        module.set("toJSONObject", new toJSONObject());
        module.set("toJSONArray", new toJSONArray());
        module.set("fromJSON", new fromJSON());
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

    private final class printCallStack extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            new Throwable().printStackTrace(pw);
            if (globals != null && globals.debuglib != null) {
                sw.append("\n------------------------------ lua ");
                sw.append(globals.debuglib.traceback(1));
            }
            logger.debug(sw.toString());
            return NIL;
        }
    }


    private final class callMethodExact extends VarArgFunction {
        /**
         * callMethodExact(instance, isStatic, methodName, { type class list ....}, ... real args )
         */
        @Override
        public Varargs invoke(Varargs args) {
            Object instance = CoerceLuaToJava.coerce(args.arg(1), Object.class);
            if (instance == null) {
                return NIL;
            }
            boolean isStatic = args.arg(2).checkboolean();
            String methodName = args.arg(3).checkjstring();
            Class[] types = (Class[]) CoerceLuaToJava.coerce(args.arg(4), Class[].class);
            if (types == null) {
                return NIL;
            }
            Varargs subVarargs = args.subargs(5);
            Object[] realArgs = LuaScript.convertTo(subVarargs);
            if (realArgs.length != types.length) {
                LuaValue.error("length of types (" + types.length + ") not equals with length of real arguments (" + realArgs.length + ")");
                return NIL;
            }
            Class[] realTypes = new Class[realArgs.length];
            for (int i = 1; i <= realArgs.length; i++) {
                realTypes[i] = realArgs[i] == null ? null : realArgs[i].getClass();
            }

            if (!ClassUtils.isAssignable(realTypes, types)) {
                LuaValue.error("cannot assign with argument types (" + Arrays.toString(realTypes) + ") in parameter types (" + Arrays.toString(types) + ")");
                return NIL;
            }

            Class cls;
            if (isStatic && instance instanceof Class) {
                cls = (Class) instance;
            } else {
                cls = instance.getClass();
            }
            Method method = null;
            try {
                method = ClassUtils.getPublicMethod(cls, methodName, types);
            } catch (NoSuchMethodException ignore) {
            }
            if (method == null) {
                LuaValue.error("cannot found method: " + methodName + " ( " + Arrays.toString(types) + " ) in " + instance);
                return NIL;
            }
            try {
                return CoerceJavaToLua.coerce(method.invoke(instance, realArgs));
            } catch (Exception e) {
                LuaValue.error(e.toString());
                return NIL;
            }
        }
    }


    private final class toJSON extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            if (arg.istable()) {
                return LuaValue.valueOf(LuaScript.convertToJSON(arg.checktable()).toString());
            } else if (arg.isuserdata()) {
                Object obj = arg.checkuserdata();
                return LuaValue.valueOf(EventBus.toJson(obj));
            } else if (arg.isnil()) {
                return NIL;
            } else {
                LuaValue.error("not support argument type:" + arg.typename());
                return NIL;
            }
        }
    }

    private final class toJSONObject extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            if (arg.istable()) {
                return LuaValue.valueOf(LuaScript.convertToJSONObject(arg.checktable()).toString());
            } else if (arg.isnil()) {
                return NIL;
            } else {
                LuaValue.error("not support argument type:" + arg.typename());
                return NIL;
            }
        }
    }

    private final class toJSONArray extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            if (arg.istable()) {
                return LuaValue.valueOf(LuaScript.convertToJSONArray(arg.checktable()).toString());
            } else if (arg.isnil()) {
                return NIL;
            } else {
                LuaValue.error("not support argument type:" + arg.typename());
                return NIL;
            }
        }
    }

    private final class fromJSON extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            if (arg.isnil())
                return NIL;

            String json = arg.checkjstring();
            StringBuilder sb = new StringBuilder();

            try {
                JSONObject jsonObject = new JSONObject(json);
                return LuaScript.convert(jsonObject);
            } catch (Throwable t) {
                sb.append(t.toString());
            }

            try {
                JSONArray jsonArray = new JSONArray(json);
                return LuaScript.convert(jsonArray);
            } catch (Throwable t) {
                sb.append(t.toString());
            }

            LuaValue.error("unpack json string failed: " + sb.toString());
            return NIL;
        }
    }

}
