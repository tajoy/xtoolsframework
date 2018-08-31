package x.tools.framework.script.lua;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

public class LuaEvent extends LuaFunction {
    LuaTable module = new LuaTable();
    LuaTable listener_map_list = new LuaTable();

    public LuaValue call(LuaValue modname, LuaValue env) {
        String namespace = "event";
        module.set("add", new add());
        module.set("remove", new remove());
        module.set("dispatch", new dispatch());
        module.set("__listener_map_list", listener_map_list);
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return module;
    }

    public LuaValue add(LuaValue arg1, LuaValue arg2) {
        String name = arg1.checkjstring();
        LuaFunction func = arg2.checkfunction();
        LuaTable list = listener_map_list.rawget(name).checktable();
        if (list == null || list.isnil()) {
            list = new LuaTable();
            listener_map_list.rawset(name, list);
        }
        int count = list.length();
        list.rawset(count, func);
        return LuaValue.TRUE;
    }

    public LuaValue remove(LuaValue arg1, LuaValue arg2) {
        String name = arg1.checkjstring();
        LuaFunction func = arg2.checkfunction();
        LuaTable list = listener_map_list.rawget(name).checktable();
        if (list == null || list.isnil()) {
            return LuaValue.FALSE;
        }
        int count = list.length();
        for (int i = 1; i <= count; i++) {
            if (func.eq_b(list.rawget(i))) {
                list.remove(i);
                return LuaValue.TRUE;
            }
        }
        return LuaValue.FALSE;
    }

    public Varargs dispatch(LuaValue _name, Varargs varargs) {
        String name = _name.checkjstring();
        LuaValue list = listener_map_list.rawget(name);
        if (list == null || list.isnil()) {
            list = new LuaTable();
            listener_map_list.rawset(name, list);
        }
        int count = list.length();
        for (int i = 1; i <= count; i++) {
            LuaValue listener = list.get(i);
            if (listener == null || listener.isnil() || !listener.isfunction()) {
                continue;
            }
            listener.invoke(varargs);
        }
        return null;
    }

    public class add extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return LuaEvent.this.add(arg1, arg2);
        }
    }

    public class remove extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return LuaEvent.this.remove(arg1, arg2);
        }
    }

    public class dispatch extends LuaFunction {
        @Override
        public Varargs invoke(Varargs varargs) {
            int nArg = varargs.narg();
            if (nArg < 1) {
                LuaValue.argerror(1, "expect string");
                return NIL;
            }
            return LuaEvent.this.dispatch(varargs.checkstring(1), varargs.subargs(2));
        }
    }
}
