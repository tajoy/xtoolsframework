package x.tools.framework.script.lua.lib;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import x.tools.framework.script.lua.LuaScript;
import x.tools.log.LogLevel;
import x.tools.log.Loggable;

public class LuaLogLib extends TwoArgFunction {
    private LuaTable module = new LuaTable();
    private Globals globals;
    private final Loggable logger = Loggable.fromName("lua-log");

    private final logger loggerTRACE = new logger(LogLevel.TRACE);
    private final logger loggerDEBUG = new logger(LogLevel.DEBUG);
    private final logger loggerINFO = new logger(LogLevel.INFO);
    private final logger loggerWARN = new logger(LogLevel.WARN);
    private final logger loggerERROR = new logger(LogLevel.ERROR);
    private final log loggerLOG = new log();

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        globals = env.checkglobals();
        String namespace = "log";

        module.set("TRACE", LuaValue.valueOf(LogLevel.TRACE.ordinal()));
        module.set("DEBUG", LuaValue.valueOf(LogLevel.DEBUG.ordinal()));
        module.set("INFO", LuaValue.valueOf(LogLevel.INFO.ordinal()));
        module.set("WARN", LuaValue.valueOf(LogLevel.WARN.ordinal()));
        module.set("ERROR", LuaValue.valueOf(LogLevel.ERROR.ordinal()));

        module.set("trace", loggerTRACE);
        module.set("t", loggerTRACE);

        module.set("debug", loggerDEBUG);
        module.set("d", loggerDEBUG);

        module.set("info", loggerINFO);
        module.set("i", loggerINFO);

        module.set("warn", loggerWARN);
        module.set("w", loggerWARN);

        module.set("error", loggerERROR);
        module.set("e", loggerERROR);

        module.set("log", loggerLOG);

        env.set(namespace, module);
        env.get("package").get("loaded").set(namespace, module);
        return env;
    }

    private Varargs logByLevel(LogLevel logLevel, Varargs args) {
        LuaValue tostring = globals.get("tostring");
        StringBuilder sb = new StringBuilder();
        Throwable throwable = null;
        for (int i = 1, n = args.narg(); i <= n; i++) {
            LuaValue arg = args.arg(i);
            if (i == 1) {
                if (arg.isuserdata()) {
                    Object object = CoerceLuaToJava.coerce(arg, Object.class);
                    if (object instanceof Throwable) {
                        throwable = (Throwable) object;
                        continue;
                    }
                }
            }
            if (i > 1) sb.append('\t');
            if (arg.istable()) {
                sb.append(LuaBaseLib.luaTableToString(tostring, arg.checktable()));
            } else {
                sb.append(tostring.call(arg).strvalue().tojstring());
            }
        }
        sb.append('\n');
        if (throwable != null) {
            logger.getLogger().log(logLevel, throwable, sb.toString());
        } else {
            logger.getLogger().log(logLevel, sb.toString());
        }
        return NONE;
    }

    private LogLevel getLogLevel(LuaValue luaValue) {
        if (luaValue.isnil()) {
            LuaValue.argerror(1, "cannot be nil");
        }
        if (luaValue.isint()) {
            int lvl = luaValue.toint();
            LogLevel logLevel = LogLevel.valueOf(lvl);
            if (logLevel == null || lvl != logLevel.ordinal()) {
                // maybe default failback value
                LuaValue.argerror(1, "unknown log level: " + lvl);
            }
            return logLevel;
        }
        if (luaValue.isstring()) {
            String lvl = luaValue.tojstring();
            LogLevel logLevel = null;
            switch (lvl.toUpperCase()) {
                case "T":
                case "TRACE": logLevel = LogLevel.TRACE; break;

                case "D":
                case "DEBUG": logLevel = LogLevel.DEBUG; break;

                case "I":
                case "INFO": logLevel = LogLevel.INFO; break;

                case "W":
                case "WARNING":
                case "WARN": logLevel = LogLevel.WARN; break;

                case "E":
                case "ERR":
                case "ERROR": logLevel = LogLevel.ERROR; break;
            }
            if (logLevel == null) {
                LuaValue.argerror(1, "unknown log level: " + lvl);
            }
            return logLevel;
        }
        LuaValue.argerror(1, "unknown log level for type: " + luaValue.typename());
        return null;
    }


    final class log extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            int count = args.narg();
            if (count < 2) {
                LuaValue.error("number of arguments at least two");
            }
            return logByLevel(getLogLevel(args.arg1()), args.subargs(2));
        }
    }

    final class logger extends VarArgFunction {
        private final LogLevel logLevel;

        logger(LogLevel logLevel) {
            this.logLevel = logLevel;
        }

        public Varargs invoke(Varargs args) {
            return logByLevel(this.logLevel, args);
        }
    }
}
