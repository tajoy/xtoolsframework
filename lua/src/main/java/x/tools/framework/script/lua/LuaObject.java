package x.tools.framework.script.lua;

import org.luaj.vm2.LuaValue;

import x.tools.framework.XContext;
import x.tools.framework.error.ScriptValueConvertError;
import x.tools.framework.script.IScriptValue;

public class LuaObject implements IScriptValue {

    private XContext xContext;
    private LuaValue luaValue;

    public LuaObject(LuaValue luaValue) {
        this.luaValue = luaValue;
    }

    public XContext getXContext() {
        return xContext;
    }

    public LuaValue getLuaValue() {
        return luaValue;
    }

    @Override
    public String getTypeName() {
        return this.luaValue.typename();
    }

    @Override
    public Object convertTo() throws ScriptValueConvertError {
        int type = this.luaValue.type();
        switch (type) {
            case LuaValue.TNONE: {
                return null;
            }
            case LuaValue.TNIL: {
                return null;
            }
            case LuaValue.TBOOLEAN: {
                return this.luaValue.toboolean();
            }
            case LuaValue.TNUMBER: {
                return this.luaValue.todouble();
            }
            case LuaValue.TSTRING: {
                return this.luaValue.tojstring();
            }
            case LuaValue.TTABLE: {
                // TODO: convert to lazy map
            }
            case LuaValue.TFUNCTION: {
                // TODO: convert to IScriptCallback
            }
            case LuaValue.TUSERDATA:
            case LuaValue.TLIGHTUSERDATA: {
                return this.luaValue.touserdata();
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


    @Override
    public <T> T convertTo(Class<T> cls) throws ScriptValueConvertError {
        int type = this.luaValue.type();
        switch (type) {
            case LuaValue.TNONE: {
                return null;
            }
            case LuaValue.TNIL: {
                return null;
            }
            case LuaValue.TBOOLEAN: {
                if (isInEquals(cls, boolean.class)) {
                    return (T) (Boolean) this.luaValue.toboolean();
                }
                break;
            }
            case LuaValue.TNUMBER: {
                if (isInEquals(cls, byte.class, Byte.class)) {
                    return (T) (Byte) this.luaValue.tobyte();
                }
                if (isInEquals(cls, short.class, Short.class)) {
                    return (T) (Short) this.luaValue.toshort();
                }
                if (isInEquals(cls, int.class, Integer.class)) {
                    return (T) (Integer) this.luaValue.toint();
                }
                if (isInEquals(cls, long.class, Long.class)) {
                    return (T) (Long) this.luaValue.tolong();
                }
                if (isInEquals(cls, float.class, Float.class)) {
                    return (T) (Float) this.luaValue.tofloat();
                }
                if (isInEquals(cls, double.class, Double.class)) {
                    return (T) (Double) this.luaValue.todouble();
                }
                break;
            }
            case LuaValue.TSTRING: {
                return (T) this.luaValue.tojstring();
            }
            case LuaValue.TTABLE: {
                // TODO: convert to lazy map
            }
            case LuaValue.TFUNCTION: {
                // TODO: convert to IScriptCallback
            }
            case LuaValue.TUSERDATA:
            case LuaValue.TLIGHTUSERDATA: {
                return (T) this.luaValue.touserdata(cls);
            }
            case LuaValue.TTHREAD: {
                // TODO: convert to Thread ?
            }
        }
        throw new ScriptValueConvertError();
    }
}
