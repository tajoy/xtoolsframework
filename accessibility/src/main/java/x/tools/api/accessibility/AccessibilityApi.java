package x.tools.api.accessibility;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiStatus;

public class AccessibilityApi extends AbstractApi {
    @Override
    public String getNamespace() {
        return "acb";
    }

    @Override
    public ApiStatus checkStatus() {
        // TODO: check permission granted and service running
        return super.checkStatus();
    }
}
