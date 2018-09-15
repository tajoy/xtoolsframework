package x.tools.api.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.Arrays;
import java.util.List;

import x.tools.api.accessibility.service.ApiService;
import x.tools.api.accessibility.service.IApiServiceProxy;
import x.tools.api.accessibility.view.ViewCondition;
import x.tools.api.accessibility.view.ViewInfo;
import x.tools.api.accessibility.view.ViewNodeInfo;
import x.tools.eventbus.rpc.RpcFactory;
import x.tools.framework.XContext;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PNonNull;
import x.tools.framework.annotation.PVarArgs;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiStatus;
import x.tools.framework.error.XError;

public class AccessibilityApi extends AbstractApi {

    private static AccessibilityApi instance = null;

    private AccessibilityApi() {
    }

    public static synchronized AccessibilityApi getInstance() {
        if (instance == null) {
            instance = new AccessibilityApi();
        }
        return instance;
    }

    @Override
    public String getNamespace() {
        return "accessibility";
    }

    private IApiServiceProxy proxy;

    @Override
    public boolean initialize(XContext xContext) throws XError {
        if (!super.initialize(xContext)) return false;
        String process = xContext.getPackageName() + ":accessibility-api-service";
        proxy = RpcFactory.getProxy(IApiServiceProxy.class, process, ApiService.class.getName());
        if (proxy == null)
            return false;
        return true;
    }

    @Override
    public ApiStatus checkStatus() {
        ApiStatus status = super.checkStatus();
        if (!ApiStatus.OK.equals(status)) {
            return status;
        }
        if (!isAccessibilityEnabled()) {
            return ApiStatus.NEED_PERMISSION;
        }
        if (!isAccessibilityRunning()) {
            return ApiStatus.NOT_RUNNING;
        }
        if (proxy == null) {
            return ApiStatus.NOT_RUNNING;
        }
        return ApiStatus.OK;
    }

    public boolean isAccessibilityRunning() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        ComponentName component = new ComponentName(this, ApiService.class);
        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
            if (component.equals(ComponentName.unflattenFromString(service.getId()))) {
                return true;
            }
        }
        return false;
    }

    public boolean isAccessibilityEnabled() {
        int accessibilityEnabled;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
        if (accessibilityEnabled == 1) {
            ComponentName component = new ComponentName(this, ApiService.class);
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                String[] list = settingValue.split(":");
                for (String it : list) {
                    if (component.equals(ComponentName.unflattenFromString(it))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**********************************************************************************************/
    /****************************************** API ***********************************************/
    /**********************************************************************************************/

    @Api
    public boolean setRootSources(
            @PName(name = "sources")
            @PVarArgs
                    RootSource... sources
    ) {
        return proxy.setRootSources(sources);
    }

    @Api
    public ViewNodeInfo getRootNodeInfo() {
        return proxy.getRootNodeInfo();
    }

    @Api
    public boolean enableRootCache() {
        return proxy.enableRootCache();
    }

    @Api
    public boolean disableRootCache() {
        return proxy.disableRootCache();
    }

    @Api
    public ViewInfo[] searchUI(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.searchUI(condition);
    }

    @Api
    public ViewInfo[] searchUI(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "root")
            @PNonNull
                    ViewNodeInfo root
    ) {
        return proxy.searchUI(condition, root);
    }

    @Api
    public ViewInfo[] searchUI(
            @PName(name = "condition")
                    ViewCondition condition,
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.searchUI(condition, viewInfo);
    }

    @Api
    public ViewInfo fetchFirstView(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.fetchFirstView(condition);
    }

    @Api
    public ViewInfo fetchFirstView(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.fetchFirstView(condition, viewInfo);
    }

    @Api
    public boolean containView(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.containView(condition);
    }

    @Api
    public boolean containAllViews(
            @PName(name = "conditions")
            @PVarArgs
                    ViewCondition... conditions
    ) {
        return proxy.containAllViews(conditions);
    }

    @Api
    public String[] rootViewNames() {
        return proxy.rootViewNames();
    }

    @Api
    public String nowActivity() {
        return proxy.nowActivity();
    }

    @Api
    public ViewInfo[] waitUntilAppear(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.waitUntilAppear(condition);
    }

    @Api
    public boolean globalAction(
            @PName(name = "actionCode")
                    int actionCode
    ) {
        return proxy.globalAction(actionCode);
    }

    @Api
    public boolean gestureTap(
            @PName(name = "x")
                    int x,
            @PName(name = "y")
                    int y,
            @PName(name = "duration")
                    int duration
    ) {
        return proxy.gestureTap(x, y, duration);
    }

    @Api
    public boolean gestureSwipe(
            @PName(name = "x1")
                    int x1,
            @PName(name = "y1")
                    int y1,
            @PName(name = "x2")
                    int x2,
            @PName(name = "y2")
                    int y2,
            @PName(name = "duration")
                    int duration
    ) {
        return proxy.gestureSwipe(x1, y1, x2, y2, duration);
    }

    @Api
    public boolean tapByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        return proxy.tapByResId(resId);
    }

    @Api
    public boolean tapExByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.tapExByResId(resId, tapTypes);
    }

    @Api
    public boolean setTextByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId,
            @PName(name = "text")
                    String text
    ) {
        return proxy.setTextByResId(resId, text);
    }

    @Api
    public boolean focusByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        return proxy.focusByResId(resId);
    }

    @Api
    public boolean tapByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.tapByText(text);
    }

    @Api
    public boolean tapExByText(
            @PName(name = "text")
            @PNonNull
                    String text,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.tapExByText(text, tapTypes);
    }

    @Api
    public boolean setTextByText(
            @PName(name = "textTarget")
            @PNonNull
                    String textTarget,
            @PName(name = "textInput")
            @PNonNull
                    String textInput
    ) {
        return proxy.setTextByText(textTarget, textInput);
    }

    @Api
    public boolean focusByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.focusByText(text);
    }

    @Api
    public boolean waitTapByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        return proxy.waitTapByResId(resId);
    }

    @Api
    public boolean waitTapExByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.waitTapExByResId(resId, tapTypes);
    }

    @Api
    public boolean waitSetTextByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId,
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.waitSetTextByResId(resId, text);
    }

    @Api
    public boolean waitFocusByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        return proxy.waitFocusByResId(resId);
    }

    @Api
    public boolean waitTapByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.waitTapByText(text);
    }

    @Api
    public boolean waitTapExByText(
            @PName(name = "text")
            @PNonNull
                    String text,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.waitTapExByText(text, tapTypes);
    }

    @Api
    public boolean waitSetTextByText(
            @PName(name = "textTarget")
            @PNonNull
                    String textTarget,
            @PName(name = "textInput")
            @PNonNull
                    String textInput
    ) {
        return proxy.waitSetTextByText(textTarget, textInput);
    }

    @Api
    public boolean waitFocusByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.waitFocusByText(text);
    }

    @Api
    public boolean tap(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.tap(viewInfo);
    }

    @Api
    public boolean tapEx(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.tapEx(viewInfo, tapTypes);
    }

    @Api
    public boolean setText(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo,
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.setText(viewInfo, text);
    }

    @Api
    public boolean focus(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.focus(viewInfo);
    }

    @Api
    public boolean scrollTo(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo,
            @PName(name = "row")
                    int row,
            @PName(name = "column")
                    int column
    ) {
        return proxy.scrollTo(viewInfo, row, column);
    }

    @Api
    public boolean tryTap(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.tryTap(condition);
    }

    @Api
    public boolean tryTapEx(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.tryTapEx(condition, tapTypes);
    }

    @Api
    public boolean trySetText(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "text")
                    String text
    ) {
        return proxy.trySetText(condition, text);
    }

    @Api
    public boolean tryFocus(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.tryFocus(condition);
    }

    @Api
    public boolean tryScrollTo(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "row")
                    int row,
            @PName(name = "column")
                    int column
    ) {
        return proxy.tryScrollTo(condition, row, column);
    }

    @Api
    public ViewInfo waitUi(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "timeout")
                    int timeout
    ) {
        return proxy.waitUi(condition, timeout);
    }

    @Api
    public ViewInfo waitUi(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.waitUi(condition);
    }

    @Api
    public boolean waitActivity(
            @PName(name = "activity")
            @PNonNull
                    String activity
    ) {
        return proxy.waitActivity(activity);
    }

    @Api
    public boolean waitActivityAndUi(
            @PName(name = "activity")
            @PNonNull
                    String activity,
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.waitActivityAndUi(activity, condition);
    }

    @Api
    public String waitActivities(
            @PName(name = "activities")
            @PVarArgs
                    String... activities
    ) {
        return proxy.waitActivities(activities);
    }

    @Api
    public ViewInfo waitUis(
            @PName(name = "conditions")
            @PVarArgs
                    ViewCondition... conditions
    ) {
        return proxy.waitUis(conditions);
    }

    @Api
    public boolean tapView(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.tapView(viewInfo);
    }

    @Api
    public boolean tapViewByGesture(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.tapViewByGesture(viewInfo);
    }

    @Api
    public boolean tapViewByParent(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.tapViewByParent(viewInfo);
    }

    @Api
    public boolean tryTapView(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        return proxy.tryTapView(viewInfo);
    }

    @Api
    public boolean tapViewEx(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.tapViewEx(viewInfo, tapTypes);
    }

    @Api
    public boolean waitTryTap(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.waitTryTap(condition);
    }

    @Api
    public boolean waitExTryTap(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "tapTypes")
            @PVarArgs
                    TapType... tapTypes
    ) {
        return proxy.waitExTryTap(condition, tapTypes);
    }

    @Api
    public boolean setTextByClipboard(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo,
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.setTextByClipboard(viewInfo, text);
    }

    @Api
    public boolean waitTrySetText(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        return proxy.waitTrySetText(condition, text);
    }

    @Api
    public boolean waitTryFocus(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        return proxy.waitTryFocus(condition);
    }

    @Api
    public boolean waitTryScrollTo(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "row")
                    int row,
            @PName(name = "column")
                    int column
    ) {
        return proxy.waitTryScrollTo(condition, row, column);
    }

    @Api
    public boolean killApp(
            @PName(name = "pkgName")
            @PNonNull
                    String pkgName
    ) {
        return proxy.killApp(pkgName);
    }

    @Api
    public boolean startApp(
            @PName(name = "pkgName")
            @PNonNull
                    String pkgName
    ) {
        return proxy.startApp(pkgName);
    }


}
