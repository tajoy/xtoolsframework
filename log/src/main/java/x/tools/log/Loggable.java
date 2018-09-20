package x.tools.log;

import java.util.Locale;

public interface Loggable {

    static Loggable fromClass(Class<?> cls) {
        return new Loggable() {
            @Override
            public ILogger getLogger() {
                return LogConfig.getLoggerFactory().getLogger(cls);
            }
        };
    }

    default ILogger getLogger() {
        return LogConfig.getLoggerFactory().getLogger(this.getClass());
    }

    default void trace(String msg) {
        getLogger().trace(msg);
    }

    default void trace(Throwable t) {
        getLogger().trace(t);
    }

    default void trace(String format, Object... arguments) {
        getLogger().trace(String.format(Locale.getDefault(), format, arguments));
    }

    default void trace(Throwable t, String msg) {
        getLogger().trace(t, msg);
    }

    default void trace(Throwable t, String format, Object... arguments) {
        getLogger().trace(t, String.format(Locale.getDefault(), format, arguments));
    }

    default void debug(String msg) {
        getLogger().debug(msg);
    }

    default void debug(Throwable t) {
        getLogger().debug(t);
    }

    default void debug(String format, Object... arguments) {
        getLogger().debug(String.format(Locale.getDefault(), format, arguments));
    }

    default void debug(Throwable t, String msg) {
        getLogger().debug(t, msg);
    }

    default void debug(Throwable t, String format, Object... arguments) {
        getLogger().debug(t, String.format(Locale.getDefault(), format, arguments));
    }

    default void info(String msg) {
        getLogger().info(msg);
    }

    default void info(Throwable t) {
        getLogger().info(t);
    }

    default void info(String format, Object... arguments) {
        getLogger().info(String.format(Locale.getDefault(), format, arguments));
    }

    default void info(Throwable t, String msg) {
        getLogger().info(t, msg);
    }

    default void info(Throwable t, String format, Object... arguments) {
        getLogger().info(t, String.format(Locale.getDefault(), format, arguments));
    }

    default void warn(String msg) {
        getLogger().warn(msg);
    }

    default void warn(Throwable t) {
        getLogger().warn(t);
    }

    default void warn(String format, Object... arguments) {
        getLogger().warn(String.format(Locale.getDefault(), format, arguments));
    }

    default void warn(Throwable t, String msg) {
        getLogger().warn(t, msg);
    }

    default void warn(Throwable t, String format, Object... arguments) {
        getLogger().warn(t, String.format(Locale.getDefault(), format, arguments));
    }

    default void error(String msg) {
        getLogger().error(msg);
    }

    default void error(Throwable t) {
        getLogger().error(t);
    }

    default void error(String format, Object... arguments) {
        getLogger().error(String.format(Locale.getDefault(), format, arguments));
    }

    default void error(Throwable t, String msg) {
        getLogger().error(t, msg);
    }

    default void error(Throwable t, String format, Object... arguments) {
        getLogger().error(t, String.format(Locale.getDefault(), format, arguments));
    }
}
