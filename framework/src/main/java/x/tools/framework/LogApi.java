package x.tools.framework;

import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.ApiConstant;
import x.tools.framework.annotation.PEnumInt;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PNonNull;
import x.tools.framework.api.AbstractApi;
import x.tools.log.ILogger;
import x.tools.log.LogConfig;
import x.tools.log.LogLevel;

public class LogApi extends AbstractApi {
    private final ILogger logger = LogConfig.getLoggerFactory().getLogger("script.api");

    @Override
    public String getNamespace() {
        return "log";
    }

    @ApiConstant
    public final int TRACE = LogLevel.TRACE.ordinal();

    @ApiConstant
    public final int DEBUG = LogLevel.DEBUG.ordinal();

    @ApiConstant
    public final int INFO = LogLevel.INFO.ordinal();

    @ApiConstant
    public final int WARN = LogLevel.WARN.ordinal();

    @ApiConstant
    public final int ERROR = LogLevel.ERROR.ordinal();

    @Api
    public void log(
            @PName(name = "level")
            @PEnumInt(target = LogLevel.class)
                    int level,

            @PName(name = "msg")
            @PNonNull
                    String msg
    ) {
        logger.log(LogLevel.valueOf(level), msg);
    }

    @Api
    public void trace(
            @PName(name = "msg")
            @PNonNull
                    String msg
    ) {
        logger.trace(msg);
    }

    @Api
    public void debug(
            @PName(name = "msg")
            @PNonNull
                    String msg
    ) {
        logger.debug(msg);
    }

    @Api
    public void info(
            @PName(name = "msg")
            @PNonNull
                    String msg
    ) {
        logger.info(msg);
    }

    @Api
    public void warn(
            @PName(name = "msg")
            @PNonNull
                    String msg
    ) {
        logger.warn(msg);
    }

    @Api
    public void error(
            @PName(name = "msg")
            @PNonNull
                    String msg
    ) {
        logger.error(msg);
    }
}
