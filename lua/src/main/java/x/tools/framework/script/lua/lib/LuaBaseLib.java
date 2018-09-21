package x.tools.framework.script.lua.lib;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
                    Object json = LuaScript.convertToJSON(arg.checktable());
                    sb.append(json.toString());
                } else {
                    LuaString s = tostring.call(arg).strvalue();
                    sb.append(s.tojstring());
                }
            }
            sb.append('\n');
            logger.info(sb.toString());
            return NONE;
        }
    }
}
