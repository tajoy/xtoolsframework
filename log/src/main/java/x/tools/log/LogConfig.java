package x.tools.log;

public final class LogConfig {
    private static ILoggerFactory loggerFactory = new DefaultLoggerFactory();

    public static ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public static void setLoggerFactory(ILoggerFactory loggerFactory) {
        LogConfig.loggerFactory = loggerFactory;
    }
}
