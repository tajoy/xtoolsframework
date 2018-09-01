package x.tools.framework;

import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PNonNull;
import x.tools.framework.api.AbstractApi;

public class XApi extends AbstractApi {

    @Override
    public String getNamespace() {
        return "x";
    }

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
