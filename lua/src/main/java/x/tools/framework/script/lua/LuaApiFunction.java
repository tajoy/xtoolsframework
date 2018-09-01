package x.tools.framework.script.lua;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Locale;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiMetaInfo;
import x.tools.framework.api.ApiStatus;
import x.tools.framework.api.ParameterMetaInfo;
import x.tools.framework.error.XError;

public class LuaApiFunction extends VarArgFunction {
    private LuaScript luaScript;
    private AbstractApi api;
    private ApiMetaInfo metaInfo;

    public LuaApiFunction(LuaScript luaScript, AbstractApi api, ApiMetaInfo metaInfo) {
        this.luaScript = luaScript;
        this.api = api;
        this.metaInfo = metaInfo;
    }

    @Override
    public Varargs invoke(Varargs args) {
        ApiStatus status = api.checkStatus();
        LuaValue.assert_(
                ApiStatus.OK.equals(status),
                String.format("Api %s status is not OK, status: %s", api.getNamespace(), status)
        );
        ParameterMetaInfo[] parameterMetaData = this.metaInfo.getParameterMetaInfo();
        int count = parameterMetaData.length;
        LuaValue.assert_(
                args.narg() == count,
                String.format(
                        Locale.getDefault(),
                        "%s.%s require %d parameter(s), but got %d parameter(s)",
                        api.getNamespace(),
                        this.metaInfo.getName(),
                        count,
                        args.narg()
                )
        );
        Object[] argObjects = new Object[count];

        for (int i = 1; i <= count; i++) {
            int ii = i - 1;
            LuaObject arg = new LuaObject(args.arg(i));
            try {
                argObjects[ii] = arg.convertTo(parameterMetaData[ii].getType());
                parameterMetaData[ii].checkAnnotation(argObjects[ii]);
            } catch (XError e) {
                LuaValue.argerror(i, "Convert error: " + e.toString());
            }
        }

        try {
            Object ret = this.metaInfo.getMethod().invoke(this.api, argObjects);
            return this.luaScript.createLuaValue(ret);
        } catch (Exception e) {
            LuaValue.error(e.toString());
        }
        return LuaValue.NIL;
    }
}
