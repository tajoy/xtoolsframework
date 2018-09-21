package x.tools.framework.script.lua.lib;

import org.json.JSONObject;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import x.tools.framework.XContext;
import x.tools.framework.script.lua.LuaScript;

public class LuaEventLib extends TwoArgFunction {
    private LuaTable module = new LuaTable();
    private LuaTable listener_map_list = new LuaTable();
    private XContext xContext;

    public LuaEventLib(XContext xContext) {
        this.xContext = xContext;
    }

    @Override
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

    public LuaValue add(LuaValue arg1, LuaValue func) {
        String name = arg1.checkjstring();
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

    public LuaValue remove(LuaValue arg1, LuaValue func) {
        String name = arg1.checkjstring();
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
            return LuaEventLib.this.add(arg1, arg2);
        }
    }

    public class remove extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return LuaEventLib.this.remove(arg1, arg2);
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
