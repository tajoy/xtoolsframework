package x.tools.framework.log;

import java.util.Locale;

import x.tools.framework.XContext;

public interface Loggable {

    default ILogger getLogger() {
        return XContext.getLoggerFactory().getLogger(this.getClass());
    }

    default void trace(String msg) {
        getLogger().trace(msg);
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
        getLogger().debug( msg);
    }

    default void debug(String format, Object... arguments) {
        getLogger().debug( String.format(Locale.getDefault(), format, arguments));
    }

    default void debug(Throwable t, String msg) {
        getLogger().debug( t, msg);
    }

    default void debug(Throwable t, String format, Object... arguments) {
        getLogger().debug( t, String.format(Locale.getDefault(), format, arguments));
    }

    default void info(String msg) {
        getLogger().info( msg);
    }

    default void info(String format, Object... arguments) {
        getLogger().info( String.format(Locale.getDefault(), format, arguments));
    }

    default void info(Throwable t, String msg) {
        getLogger().info( t, msg);
    }

    default void info(Throwable t, String format, Object... arguments) {
        getLogger().info( t, String.format(Locale.getDefault(), format, arguments));
    }

    default void warn(String msg) {
        getLogger().warn( msg);
    }

    default void warn(String format, Object... arguments) {
        getLogger().warn( String.format(Locale.getDefault(), format, arguments));
    }

    default void warn(Throwable t, String msg) {
        getLogger().warn( t, msg);
    }

    default void warn(Throwable t, String format, Object... arguments) {
        getLogger().warn( t, String.format(Locale.getDefault(), format, arguments));
    }

    default void error(String msg) {
        getLogger().error( msg);
    }

    default void error(String format, Object... arguments) {
        getLogger().error( String.format(Locale.getDefault(), format, arguments));
    }

    default void error(Throwable t, String msg) {
        getLogger().error( t, msg);
    }

    default void error(Throwable t, String format, Object... arguments) {
        getLogger().error( t, String.format(Locale.getDefault(), format, arguments));
    }
}
