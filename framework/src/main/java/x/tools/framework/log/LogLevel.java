package x.tools.framework.log;

public enum LogLevel implements Comparable<LogLevel> {

    TRACE("TRACE"),
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR"),

    ;

    private String name;

    LogLevel(String name) {
        this.name = name;
    }

    public static LogLevel valueOf(int level) {
        for (LogLevel logLevel: LogLevel.values()) {
            if (logLevel.ordinal() == level) {
                return logLevel;
            }
        }
        return TRACE;
    }

    public String getName() {
        return name;
    }

}
