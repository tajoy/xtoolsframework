package x.tools.framework.script.lua;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LuaHelper extends LuaFunction {
    LuaTable module = new LuaTable();

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        String namespace = "helper";
        module.set("createInterface", new LuaHelper.createInterface());
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return module;
    }

    private class LuaInvocationHandler implements InvocationHandler {
        private LuaTable proxyTable;

        private LuaInvocationHandler(LuaTable proxyTable) {
            this.proxyTable = proxyTable;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // TODO: proxy call to lua
            return null;
        }
    }

    public class createInterface extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            String clsName = arg1.checkjstring();
            LuaTable proxyTable = arg2.checktable();
            ClassLoader sl = ClassLoader.getSystemClassLoader();
            try {
                Class cls = sl.loadClass(clsName);
                if (!cls.isInterface()) {
                    error("bad argument: interface class name expected, got " + clsName);
                    return NIL;
                }
                Object proxyInstance = Proxy.newProxyInstance(
                        sl,
                        new Class[]{cls},
                        new LuaInvocationHandler(proxyTable)
                );
                return CoerceJavaToLua.coerce(proxyInstance);
            } catch (Exception e) {
                error(e.toString());
            }
            return NIL;
        }
    }
}
