package x.tools.api.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.Arrays;
import java.util.List;

import x.tools.api.accessibility.service.ApiService;
import x.tools.api.accessibility.view.ViewCondition;
import x.tools.api.accessibility.view.ViewInfo;
import x.tools.api.accessibility.view.ViewNodeInfo;
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

    @Override
    public boolean initialize(XContext xContext) throws XError {
        if (!super.initialize(xContext)) return false;
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
        if (ApiService.getInstance() == null) {
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
        ApiService service = ApiService.getInstance();
        return service.setRootSources(sources);
    }

    @Api
    public ViewNodeInfo getRootNodeInfo() {
        ApiService service = ApiService.getInstance();
        return service.getRootNodeInfo();
    }

    @Api
    public boolean enableRootCache() {
        ApiService service = ApiService.getInstance();
        return service.enableRootCache();
    }

    @Api
    public boolean disableRootCache() {
        ApiService service = ApiService.getInstance();
        return service.disableRootCache();
    }

    @Api
    public ViewInfo[] searchUI(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.searchUI(condition);
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
        ApiService service = ApiService.getInstance();
        return service.searchUI(condition, root);
    }

    @Api
    public ViewInfo[] searchUI(
            @PName(name = "condition")
                    ViewCondition condition,
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.searchUI(condition, viewInfo);
    }

    @Api
    public ViewInfo fetchFirstView(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.fetchFirstView(condition);
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
        ApiService service = ApiService.getInstance();
        return service.fetchFirstView(condition, viewInfo);
    }

    @Api
    public boolean containView(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.containView(condition);
    }

    @Api
    public boolean containAllViews(
            @PName(name = "conditions")
            @PVarArgs
                    ViewCondition... conditions
    ) {
        ApiService service = ApiService.getInstance();
        return service.containAllViews(conditions);
    }

    @Api
    public String[] rootViewNames() {
        ApiService service = ApiService.getInstance();
        return service.rootViewNames();
    }

    @Api
    public String nowActivity() {
        ApiService service = ApiService.getInstance();
        return service.nowActivity();
    }

    @Api
    public ViewInfo[] waitUntilAppear(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitUntilAppear(condition);
    }

    @Api
    public boolean globalAction(
            @PName(name = "actionCode")
                    int actionCode
    ) {
        ApiService service = ApiService.getInstance();
        return service.globalAction(actionCode);
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
        ApiService service = ApiService.getInstance();
        return service.gestureTap(x, y, duration);
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
        ApiService service = ApiService.getInstance();
        return service.gestureSwipe(x1, y1, x2, y2, duration);
    }

    @Api
    public boolean tapByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        ApiService service = ApiService.getInstance();
        return service.tapByResId(resId);
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
        ApiService service = ApiService.getInstance();
        return service.tapExByResId(resId, Arrays.asList(tapTypes));
    }

    @Api
    public boolean setTextByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId,
            @PName(name = "text")
                    String text
    ) {
        ApiService service = ApiService.getInstance();
        return service.setTextByResId(resId, text);
    }

    @Api
    public boolean focusByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        ApiService service = ApiService.getInstance();
        return service.focusByResId(resId);
    }

    @Api
    public boolean tapByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        ApiService service = ApiService.getInstance();
        return service.tapByText(text);
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
        ApiService service = ApiService.getInstance();
        return service.tapExByText(text, Arrays.asList(tapTypes));
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
        ApiService service = ApiService.getInstance();
        return service.setTextByText(textTarget, textInput);
    }

    @Api
    public boolean focusByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        ApiService service = ApiService.getInstance();
        return service.focusByText(text);
    }

    @Api
    public boolean waitTapByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitTapByResId(resId);
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
        ApiService service = ApiService.getInstance();
        return service.waitTapExByResId(resId, Arrays.asList(tapTypes));
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
        ApiService service = ApiService.getInstance();
        return service.waitSetTextByResId(resId, text);
    }

    @Api
    public boolean waitFocusByResId(
            @PName(name = "resId")
            @PNonNull
                    String resId
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitFocusByResId(resId);
    }

    @Api
    public boolean waitTapByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitTapByText(text);
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
        ApiService service = ApiService.getInstance();
        return service.waitTapExByText(text, Arrays.asList(tapTypes));
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
        ApiService service = ApiService.getInstance();
        return service.waitSetTextByText(textTarget, textInput);
    }

    @Api
    public boolean waitFocusByText(
            @PName(name = "text")
            @PNonNull
                    String text
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitFocusByText(text);
    }

    @Api
    public boolean tap(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.tap(viewInfo);
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
        ApiService service = ApiService.getInstance();
        return service.tapEx(viewInfo, Arrays.asList(tapTypes));
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
        ApiService service = ApiService.getInstance();
        return service.setText(viewInfo, text);
    }

    @Api
    public boolean focus(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.focus(viewInfo);
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
        ApiService service = ApiService.getInstance();
        return service.scrollTo(viewInfo, row, column);
    }

    @Api
    public boolean tryTap(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.tryTap(condition);
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
        ApiService service = ApiService.getInstance();
        return service.tryTapEx(condition, Arrays.asList(tapTypes));
    }

    @Api
    public boolean trySetText(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "text")
                    String text
    ) {
        ApiService service = ApiService.getInstance();
        return service.trySetText(condition, text);
    }

    @Api
    public boolean tryFocus(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.tryFocus(condition);
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
        ApiService service = ApiService.getInstance();
        return service.tryScrollTo(condition, row, column);
    }

    @Api
    public ViewInfo waitUi(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition,
            @PName(name = "timeout")
                    int timeout
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitUi(condition, timeout);
    }

    @Api
    public ViewInfo waitUi(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitUi(condition);
    }

    @Api
    public boolean waitActivity(
            @PName(name = "activity")
            @PNonNull
                    String activity
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitActivity(activity);
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
        ApiService service = ApiService.getInstance();
        return service.waitActivityAndUi(activity, condition);
    }

    @Api
    public String waitActivities(
            @PName(name = "activities")
            @PVarArgs
                    String... activities
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitActivities(activities);
    }

    @Api
    public ViewInfo waitUis(
            @PName(name = "conditions")
            @PVarArgs
                    ViewCondition... conditions
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitUis(conditions);
    }

    @Api
    public boolean tapView(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.tapView(viewInfo);
    }

    @Api
    public boolean tapViewByGesture(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.tapViewByGesture(viewInfo);
    }

    @Api
    public boolean tapViewByParent(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.tapViewByParent(viewInfo);
    }

    @Api
    public boolean tryTapView(
            @PName(name = "viewInfo")
            @PNonNull
                    ViewInfo viewInfo
    ) {
        ApiService service = ApiService.getInstance();
        return service.tryTapView(viewInfo);
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
        ApiService service = ApiService.getInstance();
        return service.tapViewEx(viewInfo, tapTypes);
    }

    @Api
    public boolean waitTryTap(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitTryTap(condition);
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
        ApiService service = ApiService.getInstance();
        return service.waitExTryTap(condition, Arrays.asList(tapTypes));
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
        ApiService service = ApiService.getInstance();
        return service.setTextByClipboard(viewInfo, text);
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
        ApiService service = ApiService.getInstance();
        return service.waitTrySetText(condition, text);
    }

    @Api
    public boolean waitTryFocus(
            @PName(name = "condition")
            @PNonNull
                    ViewCondition condition
    ) {
        ApiService service = ApiService.getInstance();
        return service.waitTryFocus(condition);
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
        ApiService service = ApiService.getInstance();
        return service.waitTryScrollTo(condition, row, column);
    }

    @Api
    public boolean killApp(
            @PName(name = "pkgName")
            @PNonNull
                    String pkgName
    ) {
        ApiService service = ApiService.getInstance();
        return service.killApp(pkgName);
    }

    @Api
    public boolean startApp(
            @PName(name = "pkgName")
            @PNonNull
                    String pkgName
    ) {
        ApiService service = ApiService.getInstance();
        return service.startApp(pkgName);
    }


}
