package x.tools.framework.script.lua.lib;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import x.tools.framework.XContext;
import x.tools.framework.script.lua.LuaScript;
import x.tools.log.ILogger;
import x.tools.log.LogConfig;

public class LuaBaseLib extends JseBaseLib {
    private Globals globals;
    private final XContext xContext;

    public LuaBaseLib(XContext xContext) {
        this.xContext = xContext;
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        super.call(modname, env);
        globals = env.checkglobals();
        env.set("print", new print(this));
        return env;
    }

    @Override
    public InputStream findResource(String filename) {
        if (filename == null) return null;
        File f = new File(xContext.getPathScript(filename));
        try {
            if (f.exists())
                return new FileInputStream(f);
        } catch (IOException ioe) {
            return null;
        }
        return super.findResource(filename);
    }


    static String luaTableToString(LuaValue tostring, LuaTable table) {
        return luaTableToString(new Stack<>(), tostring, table);
    }

    private static int isCycle(Stack<LuaTable> stack, LuaTable table) {
        return stack.search(table);
    }

    private static String luaTableToString(Stack<LuaTable> stack, LuaValue tostring, LuaTable table) {
        if (table == null || table.isnil()) {
            return "nil";
        }
        int cycle = isCycle(stack, table);
        if (cycle >= 0) {
            return "{ # cycle table: " + cycle + " }";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        LuaValue i = LuaValue.ZERO;
        boolean isEmpty = true;
        while (true) {
            Varargs n = table.inext(i);
            if ((i = n.arg1()).isnil())
                break;
            LuaValue v = n.arg(2);
            if (v.istable()) {
                stack.push(table);
                sb.append(luaTableToString(stack, tostring, v.checktable()));
            } else {
                sb.append(tostring.call(v).strvalue().tojstring());
            }
            sb.append(", ");
            isEmpty = false;
        }
        if (!isEmpty)
            sb.delete(sb.length() - 2, sb.length());

        isEmpty = true;
        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = table.next(k);
            if ((k = n.arg1()).isnil())
                break;

            LuaValue v = n.arg(2);

            sb.append("[");
            sb.append(k.tojstring());
            sb.append("]: ");

            if (v.istable()) {
                stack.push(table);
                sb.append(luaTableToString(stack, tostring, v.checktable()));
            } else {
                sb.append(tostring.call(v).strvalue().tojstring());
            }
            sb.append(", ");
            isEmpty = false;
        }
        if (!isEmpty)
            sb.delete(sb.length() - 2, sb.length());
        sb.append("}");
        return sb.toString();
    }

    // "print", // (...) -> void
    final class print extends VarArgFunction {
        private final ILogger logger = LogConfig.getLoggerFactory().getLogger("lua-print");
        final BaseLib baselib;

        print(BaseLib baselib) {
            this.baselib = baselib;
        }

        public Varargs invoke(Varargs args) {
            LuaValue tostring = globals.get("tostring");
            StringBuilder sb = new StringBuilder();
            for (int i = 1, n = args.narg(); i <= n; i++) {
                if (i > 1) sb.append('\t');
                LuaValue arg = args.arg(i);
                if (arg.istable()) {
                    sb.append(luaTableToString(tostring, arg.checktable()));
                } else {
                    sb.append(tostring.call(arg).strvalue().tojstring());
                }
            }
            sb.append('\n');
            logger.info(sb.toString());
            return NONE;
        }
    }
}
