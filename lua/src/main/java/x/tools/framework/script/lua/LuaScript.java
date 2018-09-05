package x.tools.framework.script.lua;


import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.jse.LuajavaLib;

import java.io.File;

import x.tools.framework.XContext;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.error.ParameterError;
import x.tools.framework.error.ScriptRuntimeError;
import x.tools.framework.error.ScriptValueConvertError;
import x.tools.framework.error.XError;
import x.tools.framework.script.IScriptCallback;
import x.tools.framework.script.IScriptEngine;
import x.tools.framework.script.IScriptValue;

public class LuaScript implements IScriptEngine {
    private boolean isInited = false;
    private XContext xContext;
    private Globals globals;
    private final LuaEvent luaEvent = new LuaEvent();


    private Globals initGlobals() {
        Globals globals = new Globals();
        globals.load(new LuaBaseLib(this.xContext));
        globals.load(new PackageLib());
        globals.load(new Bit32Lib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new CoroutineLib());
        globals.load(new JseMathLib());
        globals.load(new JseIoLib());
        globals.load(new JseOsLib());
        globals.load(new LuajavaLib());
        globals.load(luaEvent);
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
    public void runScript(String name, String script, IScriptValue... args) throws ParameterError {
        LuaValue chunk = globals.load(script, name);
        LuaValue[] argsObjects = new LuaValue[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof LuaObject) {
                argsObjects[i] = ((LuaObject) args[i]).getLuaValue();
            } else {
                throw new ParameterError("Only support use LuaObject as arguments");
            }
        }
        chunk.invoke(argsObjects);
    }

    @Override
    public void runScriptFile(String filename, IScriptValue... args) throws XError {
        LuaValue chunk = globals.loadfile(filename);
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
    public void addEventListener(String name, IScriptCallback callback) {
        luaEvent.add(LuaValue.valueOf(name), new LuaCallback(callback));
    }

    @Override
    public void removeEventListener(String name, IScriptCallback callback) {
        luaEvent.remove(LuaValue.valueOf(name), new LuaCallback(callback));
    }

    @Override
    public IScriptValue dispatchEvent(String name, IScriptValue... args) throws XError {
        LuaValue[] luaValues = new LuaValue[args.length];
        for (int i = 0; i < args.length; i++) {
            IScriptValue arg = args[i];
            if (arg instanceof LuaObject) {
                luaValues[i] = ((LuaObject) arg).getLuaValue();
            } else {
                throw new ScriptValueConvertError("return value is not created by LuaScript");
            }
        }
        Varargs varargs = luaEvent.dispatch(LuaValue.valueOf(name), LuaValue.varargsOf(luaValues));
        int count = varargs.narg();
        LuaValue ret;
        switch (count) {
            case 0:
                ret = LuaValue.NIL;
                break;
            case 1:
                ret = varargs.arg(1);
                break;
            default: {
                LuaTable luaTable = new LuaTable();
                for (int i = 1; i <= count; i++) {
                    luaTable.rawset(i, varargs.arg(i));
                }
                ret = luaTable;
            }
        }
        return new LuaObject(ret);
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
        int type = luaValue.type();
        switch (type) {
            case LuaValue.TNONE: {
                return null;
            }
            case LuaValue.TNIL: {
                return null;
            }
            case LuaValue.TBOOLEAN: {
                return luaValue.toboolean();
            }
            case LuaValue.TNUMBER: {
                return luaValue.todouble();
            }
            case LuaValue.TSTRING: {
                return luaValue.tojstring();
            }
            case LuaValue.TTABLE: {
                // TODO: convert to lazy map
            }
            case LuaValue.TFUNCTION: {
                // TODO: convert to IScriptCallback
            }
            case LuaValue.TUSERDATA:
            case LuaValue.TLIGHTUSERDATA: {
                return luaValue.touserdata();
            }
            case LuaValue.TTHREAD: {
                // TODO: convert to Thread ?
            }
        }
        throw new ScriptValueConvertError();
    }

    private static boolean isInEquals(Object obj, Object... args) {
        if (obj == null) return false;
        if (args == null || args.length <= 0) return false;
        for (Object arg : args) {
            if (obj.equals(arg))
                return true;
        }
        return false;
    }

    public static <T> T convertTo(LuaValue luaValue, Class<T> cls) throws ScriptValueConvertError {
        int type = luaValue.type();
        switch (type) {
            case LuaValue.TNONE: {
                return null;
            }
            case LuaValue.TNIL: {
                return null;
            }
            case LuaValue.TBOOLEAN: {
                if (isInEquals(cls, boolean.class)) {
                    return (T) (Boolean) luaValue.toboolean();
                }
                break;
            }
            case LuaValue.TNUMBER: {
                if (isInEquals(cls, byte.class, Byte.class)) {
                    return (T) (Byte) luaValue.tobyte();
                }
                if (isInEquals(cls, short.class, Short.class)) {
                    return (T) (Short) luaValue.toshort();
                }
                if (isInEquals(cls, int.class, Integer.class)) {
                    return (T) (Integer) luaValue.toint();
                }
                if (isInEquals(cls, long.class, Long.class)) {
                    return (T) (Long) luaValue.tolong();
                }
                if (isInEquals(cls, float.class, Float.class)) {
                    return (T) (Float) luaValue.tofloat();
                }
                if (isInEquals(cls, double.class, Double.class)) {
                    return (T) (Double) luaValue.todouble();
                }
                break;
            }
            case LuaValue.TSTRING: {
                return (T) luaValue.tojstring();
            }
            case LuaValue.TTABLE: {
                // TODO: convert to lazy map
            }
            case LuaValue.TFUNCTION: {
                // TODO: convert to IScriptCallback
            }
            case LuaValue.TUSERDATA:
            case LuaValue.TLIGHTUSERDATA: {
                return (T) luaValue.touserdata(cls);
            }
            case LuaValue.TTHREAD: {
                // TODO: convert to Thread ?
            }
        }
        throw new ScriptValueConvertError();
    }


    public static LuaValue varargs2LuaValue(Varargs varargs) {
        if (varargs == null)
            return LuaValue.NIL;
        int count = varargs.narg();
        switch (count) {
            case 0: return LuaValue.NIL;
            case 1: return varargs.arg1();
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
}
