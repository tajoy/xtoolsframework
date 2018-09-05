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
        return LuaScript.convertTo(this.luaValue);
    }

    @Override
    public <T> T convertTo(Class<T> cls) throws ScriptValueConvertError {
        return LuaScript.convertTo(this.luaValue, cls);
    }

}
