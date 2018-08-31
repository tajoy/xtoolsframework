package x.tools.framework.script.lua;


import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import x.tools.framework.error.ScriptValueConvertError;
import x.tools.framework.script.IScriptCallback;
import x.tools.framework.script.IScriptValue;

public class LuaCallback extends LuaFunction {
    private final IScriptCallback callback;

    public LuaCallback(IScriptCallback callback) {
        this.callback = callback;
    }

    @Override
    public Varargs invoke(Varargs varargs) {
        int nArg = varargs.narg();
        Object[] objects = new Object[nArg];
        for (int i = 2; i < nArg; i++) {
            try {
                objects[i] = new LuaObject(varargs.arg(i)).convertTo();
            } catch (ScriptValueConvertError e) {
                LuaValue.argerror(i, e.toString());
            }
        }
        Object ret = callback.call(varargs.checkjstring(1), objects);
        return CoerceJavaToLua.coerce(ret);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LuaCallback) {
            LuaCallback other = (LuaCallback) obj;
            if (this.callback.equals(other.callback)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean eq_b(LuaValue val) {
        return this.equals(val);
    }

    @Override
    public LuaValue eq(LuaValue val) {
        return this.equals(val) ? LuaValue.TRUE : LuaValue.FALSE;
    }

}
