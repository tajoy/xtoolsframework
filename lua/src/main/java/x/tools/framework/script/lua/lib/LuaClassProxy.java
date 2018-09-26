package x.tools.framework.script.lua.lib;

import android.content.Context;

import com.android.dx.DexMaker;
import com.android.dx.stock.ProxyBuilder;

import org.apache.commons.lang3.ClassUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import x.tools.framework.script.lua.LuaScript;
import x.tools.log.Loggable;

public class LuaClassProxy extends TwoArgFunction {
    private static final Loggable logger = Loggable.fromName("lua-proxy");
    private Globals globals;
    private LuaTable module = new LuaTable();
    private Context context;

    public LuaClassProxy(Context context) {
        this.context = context;
    }

    private File getOutputDir() {
        File outputDir = null;
        Context context = null;
        LuaValue _context = globals.get("context");
        if (!_context.isnil()) {
            context = (Context) CoerceLuaToJava.coerce(_context, Context.class);
        }
        if (context == null) {
            context = this.context;
        }
        if (context != null) {
            outputDir = context.getDir("dex-maker", Context.MODE_PRIVATE);
            if (!outputDir.canWrite()) {
                outputDir = null;
            }
        }
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs();
        }
        return outputDir;
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        globals = env.checkglobals();
        String namespace = "proxy";
        module.set("create", new create());
        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return env;
    }

    private class create extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue argClass, LuaValue argProxy) {
            LuaTable proxyTable = argProxy.checktable();
            Class<?> cls = null;
            ClassLoader classLoader = null;
            LuaValue _classLoader = globals.get("classLoader");
            if (!_classLoader.isnil()) {
                try {
                    classLoader = (ClassLoader) CoerceLuaToJava.coerce(_classLoader, ClassLoader.class);
                } catch (Throwable ignore) {
                }
            }
            if (argClass.isstring()) {
                try {
                    if (classLoader != null) {
                        cls = ClassUtils.getClass(classLoader, argClass.checkjstring());
                    } else {
                        cls = ClassUtils.getClass(argClass.checkjstring());
                    }
                } catch (ClassNotFoundException e) {
                    LuaValue.error(e.toString() + ": " + argClass);
                }
            } else {
                cls = (Class<?>) CoerceLuaToJava.coerce(argClass, Class.class);
            }
            if (cls.isInterface()) {
                Object proxy;
                if (classLoader != null) {
                    proxy = Proxy.newProxyInstance(classLoader, new Class[]{cls}, new ProxyHandler(proxyTable));
                } else {
                    proxy = Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new ProxyHandler(proxyTable));
                }
                return CoerceJavaToLua.coerce(proxy);
            } else {
                try {
                    Method[] methods = getAllProxyMethods(cls, proxyTable);
                    logger.trace("output dir: %s", getOutputDir());
                    logger.trace("proxy methods: %s", Arrays.toString(methods));
                    ProxyBuilder builder = ProxyBuilder.forClass(cls);
                    if (classLoader != null)
                        builder.parentClassLoader(classLoader);
                    builder.dexCache(getOutputDir());
                    builder.handler(new ProxyHandler(proxyTable));
                    builder.onlyMethods(methods);
                    return CoerceJavaToLua.coerce(builder.build());
                } catch (Exception e) {
                    LuaValue.error(e.toString() + ": " + argClass);
                }
            }
            return NIL;
        }

        private Method[] getAllProxyMethods(Class cls, LuaValue proxyTable) {
            List<Method> proxyMethods = new ArrayList<>();
            for (Method m : cls.getDeclaredMethods()) {
                String methodName = m.getName();
                if (!proxyTable.get(methodName).isnil()) {
                    proxyMethods.add(m);
                }
            }
            for (Class itCls : ClassUtils.getAllSuperclasses(cls)) {
                for (Method m : itCls.getDeclaredMethods()) {
                    String methodName = m.getName();
                    if (!proxyTable.get(methodName).isnil()) {
                        proxyMethods.add(m);
                    }
                }
            }
            for (Class itCls : ClassUtils.getAllInterfaces(cls)) {
                for (Method m : itCls.getDeclaredMethods()) {
                    String methodName = m.getName();
                    if (!proxyTable.get(methodName).isnil()) {
                        proxyMethods.add(m);
                    }
                }
            }
            return proxyMethods.toArray(new Method[proxyMethods.size()]);
        }
    }

    private class ProxyHandler implements InvocationHandler {
        private final LuaValue proxyTable;

        private ProxyHandler(LuaValue proxyTable) {
            this.proxyTable = proxyTable;
        }

        private static final int METHOD_MODIFIERS_VARARGS = 0x80;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            LuaValue fn = proxyTable.get(method.getName());
            boolean isvarargs = ((method.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
            int n = args != null ? args.length : 0;
            LuaValue[] v;
            if (isvarargs) {
                Object o = args[--n];
                int m = Array.getLength(o);
                v = new LuaValue[n + m];
                for (int i = 0; i < n; i++)
                    v[i] = CoerceJavaToLua.coerce(args[i]);
                for (int i = 0; i < m; i++)
                    v[i + n] = CoerceJavaToLua.coerce(Array.get(o, i));
            } else {
                v = new LuaValue[n];
                for (int i = 0; i < n; i++)
                    v[i] = CoerceJavaToLua.coerce(args[i]);
            }
            LuaValue[] luaArgs = new LuaValue[v.length + 3];
            luaArgs[0] = CoerceJavaToLua.coerce(proxy);
            luaArgs[1] = new CallSuper(proxy, method);
            luaArgs[2] = CoerceJavaToLua.coerce(method);
            System.arraycopy(v, 0, luaArgs, 3, v.length);
            return LuaScript.convertTo(fn.invoke(luaArgs).arg1(), method.getReturnType());
        }
    }

    private class CallSuper extends VarArgFunction {
        private final Object proxy;
        private final Method method;

        private CallSuper(Object proxy, Method method) {
            this.proxy = proxy;
            this.method = method;
        }

        private Varargs adjustArgs(Varargs args) {
            int np = method.getParameterTypes().length;
            LuaValue[] newArgs = new LuaValue[np];

            for (int i = 1; i <= np; i++) {
                newArgs[i - 1] = args.arg(i);
            }

            return varargsOf(newArgs);
        }

        @Override
        public Varargs invoke(Varargs args) {
            Object[] realArgs = LuaScript.convertTo(adjustArgs(args), method.getParameterTypes());
            try {
                return CoerceJavaToLua.coerce(ProxyBuilder.callSuper(proxy, method, realArgs));
            } catch (Throwable throwable) {
                LuaValue.error(throwable.getMessage());
            }
            return NIL;
        }
    }
}