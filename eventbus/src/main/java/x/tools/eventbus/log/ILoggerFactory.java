package x.tools.eventbus.log;

public interface ILoggerFactory {
    ILogger getLogger(String name);
    default ILogger getLogger(Class<?> cls) {
        return getLogger(cls.getName());
    }
}
