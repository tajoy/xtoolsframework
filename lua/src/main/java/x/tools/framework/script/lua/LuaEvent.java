package x.tools.framework.script.lua;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.Iterator;

import x.tools.framework.XContext;
import x.tools.framework.event.EventBus;

public class LuaEvent extends LuaFunction {
    private LuaTable module = new LuaTable();
    private LuaTable listener_map_list = new LuaTable();
    private XContext xContext;

    public LuaEvent(XContext xContext) {
        this.xContext = xContext;
    }

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
        LuaTable list;
        LuaValue v = listener_map_list.rawget(name);
        if (v == null || v.isnil() || !v.istable()) {
            list = new LuaTable();
            listener_map_list.rawset(name, list);
        } else {
            list = v.checktable();
        }
        int count = list.length();
        list.rawset(count + 1, func);
        return LuaValue.TRUE;
    }

    public LuaValue remove(LuaValue arg1, LuaValue arg2) {
        String name = arg1.checkjstring();
        LuaFunction func = arg2.checkfunction();
        LuaValue v = listener_map_list.rawget(name);
        if (v == null || v.isnil() || !v.istable()) {
            return LuaValue.FALSE;
        }
        LuaTable list = v.checktable();
        int count = list.length();
        for (int i = 1; i <= count; i++) {
            if (func.eq_b(list.rawget(i))) {
                list.remove(i);
                return LuaValue.TRUE;
            }
        }
        return LuaValue.FALSE;
    }

    public void dispatch(LuaValue _name, LuaValue data) {
        String name = _name.checkjstring();
        LuaValue v = listener_map_list.rawget(name);
        if (v == null || v.isnil() || !v.istable()) {
            return;
        }
        LuaTable list = v.checktable();
        int count = list.length();
        for (int i = 1; i <= count; i++) {
            LuaValue listener = list.get(i);
            if (listener == null || listener.isnil() || !listener.isfunction()) {
                continue;
            }
            listener.invoke(_name, data);
        }
    }

    public void dispatch(String name, JSONObject data) {
        dispatch(LuaValue.valueOf(name), LuaScript.convert(data));
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

    public class dispatch extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            if (arg2.isnil()) {
                xContext.trigger(arg1.checkjstring());
            } else {
                xContext.triggerRaw(arg1.checkjstring(), LuaScript.convertToJSONObject(arg2.checktable()));
            }
            return NIL;
        }
    }
}
