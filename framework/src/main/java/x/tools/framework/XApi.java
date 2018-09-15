package x.tools.framework;

import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.ApiConstant;
import x.tools.framework.annotation.PName;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiStatus;

public class XApi extends AbstractApi {

    @Override
    public String getNamespace() {
        return "x";
    }

    @ApiConstant
    public final int OK = ApiStatus.OK.ordinal();

    @ApiConstant
    public final int NOT_INIT = ApiStatus.NOT_INIT.ordinal();

    @ApiConstant
    public final int INIT_FAIL = ApiStatus.INIT_FAIL.ordinal();

    @ApiConstant
    public final int NEED_PERMISSION = ApiStatus.NEED_PERMISSION.ordinal();

    @ApiConstant
    public final int NOT_RUNNING = ApiStatus.NOT_RUNNING.ordinal();

    @ApiConstant
    public final int OTHER_ERROR = ApiStatus.OTHER_ERROR.ordinal();

    @Api
    public String getPathScript() {
        return xContext.getPathScript();
    }

    @Api
    public String getPathTemp() {
        return xContext.getPathTemp();
    }

    @Api
    public String getPathData() {
        return xContext.getPathData();
    }

    @Api
    public String getPathScript(
            @PName(name = "subPath")
                    String subPath
    ) {
        return xContext.getPathScript(subPath);
    }

    @Api
    public String getPathTemp(
            @PName(name = "subPath")
                    String subPath
    ) {
        return xContext.getPathTemp(subPath);
    }

    @Api
    public String getPathData(
            @PName(name = "subPath")
                    String subPath
    ) {
        return xContext.getPathData(subPath);
    }

    @Api
    public void delay(
            @PName(name = "duration")
                    long duration
    ) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ignore) {
        }
    }
}
