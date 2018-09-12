package x.tools.eventbus.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultLoggerFactory implements ILoggerFactory {

    private static final ConcurrentMap<String, ILogger> cacheLoggers = new ConcurrentHashMap<>();

    @Override
    public ILogger getLogger(String name) {
        synchronized (cacheLoggers) {
            ILogger logger = cacheLoggers.get(name);
            if (logger == null) {
                logger = new DefaultLogger(name);
                cacheLoggers.put(name, logger);
            }
            return logger;
        }
    }

    @Override
    public ILogger getLogger(Class<?> cls) {
        return getLogger(cls.getName());
    }
}
