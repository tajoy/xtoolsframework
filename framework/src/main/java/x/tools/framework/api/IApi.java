package x.tools.framework.api;

import x.tools.framework.annotation.Api;

public interface IApi {

    @Api(name = "checkStatus")
    int _checkStatus();

    @Api
    String statusDescription();

    @Api
    boolean isOk();

}
