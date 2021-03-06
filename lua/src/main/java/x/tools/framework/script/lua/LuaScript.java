package x.tools.framework.script.lua;


import android.os.Looper;

import org.apache.commons.lang3.ClassUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;
import org.luaj.vm2.lib.jse.LuajavaLib;

import java.util.Iterator;

import x.tools.framework.XContext;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.error.ParameterError;
import x.tools.framework.error.ScriptRuntimeError;
import x.tools.framework.error.ScriptValueConvertError;
import x.tools.framework.error.XError;
import x.tools.framework.script.IScriptEngine;
import x.tools.framework.script.IScriptValue;
import x.tools.framework.script.lua.lib.LuaBaseLib;
import x.tools.framework.script.lua.lib.LuaClassProxy;
import x.tools.framework.script.lua.lib.LuaEventLib;
import x.tools.framework.script.lua.lib.LuaLogLib;
import x.tools.framework.script.lua.lib.LuaLoopLib;
import x.tools.framework.script.lua.lib.LuaTimerLib;
import x.tools.framework.script.lua.lib.LuaUtilLib;

public class LuaScript implements IScriptEngine {
    private boolean isInited = false;
    private XContext xContext;
    private Globals globals;
    private LuaEventLib luaEventLib;


    private Globals initGlobals() {
        LuaString.s_metatable = new LuaTable();
        LuaNumber.s_metatable = new LuaTable();
        LuaBoolean.s_metatable = new LuaTable();
        LuaFunction.s_metatable = new LuaTable();

        Globals globals = new Globals();
        globals.load(new PackageLib()); // must be first called
        globals.load(new LuaBaseLib(this.xContext));
        globals.load(new DebugLib());
        globals.load(new Bit32Lib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new CoroutineLib());
        globals.load(new JseMathLib());
        globals.load(new JseIoLib());
        globals.load(new JseOsLib());
        globals.load(new LuajavaLib());
        globals.load(new LuaLogLib());
        globals.load(new LuaClassProxy(this.xContext));
        globals.load(this.luaEventLib = new LuaEventLib(xContext));
        globals.load(new LuaTimerLib());
        globals.load(new LuaUtilLib());
        globals.load(new LuaLoopLib(this.xContext.getMainLooper()));
        LoadState.install(globals);
        LuaC.install(globals);
        return globals;
    }


    @Override
    public void init(XContext xContext) {
        if (isInited) return;
        this.xContext = xContext;
        this.globals = initGlobals();
        this.isInited = true;
    }

    @Override
    public void registerApi(AbstractApi abstractApi) {
        globals.load(new LuaApiModule(this, abstractApi));
    }

    @Override
    public void runScript(String name, String script, IScriptValue... args) throws XError {
        LuaValue chunk;
        try {
            chunk = globals.load(script);
        } catch (LuaError e) {
            throw new ScriptRuntimeError(e);
        }
        LuaValue[] argsObjects = new LuaValue[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof LuaObject) {
                argsObjects[i] = ((LuaObject) args[i]).getLuaValue();
            } else {
                throw new ParameterError("Only support use LuaObject as arguments");
            }
        }
        try {
            chunk.invoke(argsObjects);
        } catch (LuaError e) {
            throw new ScriptRuntimeError(e);
        }
    }

    @Override
    public void runScriptFile(String filename, IScriptValue... args) throws XError {
        LuaValue chunk;
        try {
            chunk = globals.loadfile(filename);
        } catch (LuaError e) {
            throw new ScriptRuntimeError(e);
        }
        LuaValue[] argsObjects = new LuaValue[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof LuaObject) {
                argsObjects[i] = ((LuaObject) args[i]).getLuaValue();
            } else {
                throw new ParameterError("Only support use LuaObject as arguments");
            }
        }
        try {
            chunk.invoke(argsObjects);
        } catch (LuaError e) {
            throw new ScriptRuntimeError(e);
        }
    }

    @Override
    public void dispatchEvent(String name, JSONObject data) throws XError {
        luaEventLib.dispatch(name, data);
    }

    public static LuaValue createLuaValue(Object value) throws ScriptValueConvertError {
        return CoerceJavaToLua.coerce(value);
    }

    public static LuaValue[] createLuaValues(Object[] values) throws ScriptValueConvertError {
        LuaValue[] ret = new LuaValue[values.length];
        for (int i = 0; i < values.length; i++) {
            ret[i] = CoerceJavaToLua.coerce(values[i]);
        }
        return ret;
    }

    public static Object convertTo(LuaValue luaValue) throws ScriptValueConvertError {
        return CoerceLuaToJava.coerce(luaValue, Object.class);
    }

    public static <T> T convertTo(LuaValue luaValue, Class<T> cls) throws ScriptValueConvertError {
        return (T) CoerceLuaToJava.coerce(luaValue, cls);
    }

    public static Object[] convertTo(Varargs args) {
        Object[] ret = new Object[args.narg()];
        for (int i = 1; i <= args.narg(); i++) {
            ret[i - 1] = CoerceLuaToJava.coerce(args.arg(i), Object.class);
        }
        return ret;
    }

    public static Object[] convertTo(Varargs args, Class<?>[] clsArray) {
        Object[] ret = new Object[args.narg()];
        for (int i = 1; i <= args.narg(); i++) {
            Class cls = Object.class;
            if (clsArray != null && i - 1 < clsArray.length) {
                cls = clsArray[i - 1];
            }
            ret[i - 1] = CoerceLuaToJava.coerce(args.arg(i), cls);
        }
        return ret;
    }

    public static LuaValue varargs2LuaValue(Varargs varargs) {
        if (varargs == null)
            return LuaValue.NIL;
        int count = varargs.narg();
        switch (count) {
            case 0:
                return LuaValue.NIL;
            case 1:
                return varargs.arg1();
            default: {
                LuaTable list = new LuaTable();
                for (int i = 1; i < count; i++) {
                    list.set(i, varargs.arg(i));
                }
                return list;
            }
        }
    }

    @Override
    public <T> IScriptValue createValue(T value) throws XError {
        return new LuaObject(createLuaValue(value));
    }

    public static LuaTable convert(JSONArray data) {
        LuaTable ret = new LuaTable();
        if (data != null) {
            int length = data.length();
            for (int i = 0; i < length; i++) {
                try {
                    Object value = data.get(i);
                    if (value == null || JSONObject.NULL.equals(value))
                        continue;
                    if (ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
                        ret.set(i + 1, LuaScript.createLuaValue(value));
                    } else {
                        if (value instanceof Number) {
                            ret.set(i + 1, LuaScript.createLuaValue(value));
                        }
                        if (value instanceof JSONArray) {
                            ret.set(i + 1, convert((JSONArray) value));
                        }
                        if (value instanceof JSONObject) {
                            ret.set(i + 1, convert((JSONObject) value));
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return ret;
    }

    public static LuaTable convert(JSONObject data) {
        LuaTable ret = new LuaTable();
        if (data != null) {
            Iterator<String> iterator = data.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    Object value = data.get(key);
                    if (value == null || JSONObject.NULL.equals(value))
                        continue;

                    if (ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
                        ret.set(key, LuaScript.createLuaValue(value));
                    } else {
                        if (value instanceof Number) {
                            ret.set(key, LuaScript.createLuaValue(value));
                        }
                        if (value instanceof JSONArray) {
                            ret.set(key, convert((JSONArray) value));
                        }
                        if (value instanceof JSONObject) {
                            ret.set(key, convert((JSONObject) value));
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return ret;
    }

    public static boolean isArray(LuaTable table) {
        LuaValue i = LuaValue.ZERO;
        int arrayCount = 0;
        while (true) {
            Varargs n = table.inext(i);
            if ((i = n.arg1()).isnil())
                break;
            arrayCount++;
        }

        LuaValue k = LuaValue.NIL;
        int keyCount = 0;
        while (true) {
            Varargs n = table.next(k);
            if ((k = n.arg1()).isnil())
                break;
            keyCount++;
        }
        return arrayCount == keyCount;
    }

    public static Object convertToJSON(LuaTable table) {
        if (isArray(table)) {
            return convertToJSONArray(table);
        } else {
            return convertToJSONObject(table);
        }
    }

    public static JSONArray convertToJSONArray(LuaTable data) {
        JSONArray ret = new JSONArray();
        if (data != null && !data.isnil()) {
            LuaValue i = LuaValue.ZERO;
            while (true) {
                Varargs n = data.inext(i);
                if ((i = n.arg1()).isnil())
                    break;
                try {
                    int index = i.checkint();
                    LuaValue value = n.arg(2);
                    if (value.isnil()) {
                        continue;
                    }
                    if (value.istable()) {
                        ret.put(index - 1, convertToJSON(value.checktable()));
                    } else {
                        Object object = convertTo(value);
                        if (object == null) {
                            continue;
                        }
                        Class cls = object.getClass();
                        if (ClassUtils.isPrimitiveOrWrapper(cls) || ClassUtils.isAssignable(cls, String.class)) {
                            ret.put(index - 1, object);
                        } else {
                            ret.put(index - 1, new JSONObject(XContext.toJson(object)));
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return ret;
    }

    public static JSONObject convertToJSONObject(LuaTable data) {
        JSONObject ret = new JSONObject();
        if (data != null && !data.isnil()) {
            LuaValue k = LuaValue.NIL;
            while (true) {
                Varargs n = data.next(k);
                if ((k = n.arg1()).isnil())
                    break;
                try {
                    String key = convertTo(k).toString();
                    LuaValue value = n.arg(2);
                    if (value.isnil()) {
                        continue;
                    }
                    if (value.istable()) {
                        ret.put(key, convertToJSON(value.checktable()));
                    } else {
                        Object object = LuaScript.convertTo(value);
                        if (object == null) {
                            continue;
                        }
                        Class cls = object.getClass();
                        if (ClassUtils.isPrimitiveOrWrapper(cls) || ClassUtils.isAssignable(cls, String.class)) {
                            ret.put(key, object);
                        } else {
                            ret.put(key, new JSONObject(XContext.toJson(object)));
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return ret;
    }
}
