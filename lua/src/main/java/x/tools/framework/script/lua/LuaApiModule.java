package x.tools.framework.script.lua;


import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.lang.reflect.Field;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiMetaInfo;
import x.tools.framework.error.ScriptValueConvertError;

public class LuaApiModule extends LuaFunction {
    private LuaScript luaScript;
    private AbstractApi api;

    public LuaApiModule(LuaScript luaScript, AbstractApi api) {
        this.luaScript = luaScript;
        this.api = api;
    }

    public LuaValue call(LuaValue modname, LuaValue env) {
        String namespace = api.getNamespace();
        LuaTable module = new LuaTable();
        ApiMetaInfo[] apiMetaInfo = api.getApiMetaInfo();
        int count = apiMetaInfo != null ? apiMetaInfo.length : 0;
        for (int i = 0; i < count; i++) {
            ApiMetaInfo metaInfo = apiMetaInfo[i];
            if (metaInfo.getMethod() != null) {
                module.set(metaInfo.getName(), new LuaApiFunction(luaScript, api, metaInfo));
            }
            Field field = metaInfo.getField();
            if (field != null) {
                try {
                    field.setAccessible(true);
                    module.set(metaInfo.getName(), luaScript.createLuaValue(field.get(api)));
                } catch (ScriptValueConvertError | IllegalAccessException e) {
                    LuaValue.error(e.getMessage());
                }
            }
        }
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return module;
    }
}
