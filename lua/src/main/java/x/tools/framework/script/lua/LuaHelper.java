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
            LuaFunction func = this.proxyTable.get(method.getName()).checkfunction();
            LuaValue ret = LuaScript.varargs2LuaValue(func.invoke(
                    LuaScript.createLuaValue(proxy),
                    LuaValue.varargsOf(LuaScript.createLuaValues(args))
            ));
            return LuaScript.convertTo(ret, method.getReturnType());
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


    public class createInterfaces extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            LuaTable clsNames = arg1.checktable();
            LuaTable proxyTable = arg2.checktable();
            ClassLoader sl = ClassLoader.getSystemClassLoader();
            try {
                int length = clsNames.length();
                if (length <= 0) {
                    error("bad argument: interface class name array, got " + clsNames);
                    return NIL;
                }
                Class[] interfaces = new Class[length];
                for (int i = 1; i <= length; i++) {
                    int ii = i - 1;
                    String clsName = clsNames.checkjstring(i);
                    Class cls = sl.loadClass(clsName);
                    if (!cls.isInterface()) {
                        error("bad argument: interface class name expected, got " + clsName);
                        return NIL;
                    }
                    interfaces[ii] = cls;
                }
                Object proxyInstance = Proxy.newProxyInstance(
                        sl,
                        interfaces,
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
