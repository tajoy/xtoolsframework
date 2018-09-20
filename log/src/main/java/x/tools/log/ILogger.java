package x.tools.log;

import java.util.Locale;

public interface ILogger {

    String getName();

    default void trace(String msg) {
        log(LogLevel.TRACE, msg);
    }

    default void trace(String format, Object... arguments) {
        log(LogLevel.TRACE, String.format(Locale.getDefault(), format, arguments));
    }

    default void trace(Throwable t) {
        log(LogLevel.TRACE, t);
    }

    default void trace(Throwable t, String msg) {
        log(LogLevel.TRACE, t, msg);
    }

    default void trace(Throwable t, String format, Object... arguments) {
        log(LogLevel.TRACE, t, String.format(Locale.getDefault(), format, arguments));
    }

    default void debug(String msg) {
        log(LogLevel.DEBUG, msg);
    }

    default void debug(String format, Object... arguments) {
        log(LogLevel.DEBUG, String.format(Locale.getDefault(), format, arguments));
    }

    default void debug(Throwable t) {
        log(LogLevel.DEBUG, t);
    }

    default void debug(Throwable t, String msg) {
        log(LogLevel.DEBUG, t, msg);
    }

    default void debug(Throwable t, String format, Object... arguments) {
        log(LogLevel.DEBUG, t, String.format(Locale.getDefault(), format, arguments));
    }

    default void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    default void info(String format, Object... arguments) {
        log(LogLevel.INFO, String.format(Locale.getDefault(), format, arguments));
    }

    default void info(Throwable t) {
        log(LogLevel.INFO, t);
    }

    default void info(Throwable t, String msg) {
        log(LogLevel.INFO, t, msg);
    }

    default void info(Throwable t, String format, Object... arguments) {
        log(LogLevel.INFO, t, String.format(Locale.getDefault(), format, arguments));
    }

    default void warn(String msg) {
        log(LogLevel.WARN, msg);
    }

    default void warn(String format, Object... arguments) {
        log(LogLevel.WARN, String.format(Locale.getDefault(), format, arguments));
    }

    default void warn(Throwable t) {
        log(LogLevel.WARN, t);
    }

    default void warn(Throwable t, String msg) {
        log(LogLevel.WARN, t, msg);
    }

    default void warn(Throwable t, String format, Object... arguments) {
        log(LogLevel.WARN, t, String.format(Locale.getDefault(), format, arguments));
    }

    default void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    default void error(String format, Object... arguments) {
        log(LogLevel.ERROR, String.format(Locale.getDefault(), format, arguments));
    }

    default void error(Throwable t) {
        log(LogLevel.ERROR, t);
    }

    default void error(Throwable t, String msg) {
        log(LogLevel.ERROR, t, msg);
    }

    default void error(Throwable t, String format, Object... arguments) {
        log(LogLevel.ERROR, t, String.format(Locale.getDefault(), format, arguments));
    }

    default void log(LogLevel logLevel, Throwable throwable) {
        log(logLevel, throwable, "");
    }

    void log(LogLevel logLevel, String msg);

    void log(LogLevel logLevel, Throwable throwable, String msg);

}
