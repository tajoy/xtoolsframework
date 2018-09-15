package x.tools.api.accessibility.service;

import x.tools.api.accessibility.RootSource;
import x.tools.api.accessibility.TapType;
import x.tools.api.accessibility.view.ViewCondition;
import x.tools.api.accessibility.view.ViewInfo;
import x.tools.api.accessibility.view.ViewNodeInfo;

public interface IApiServiceProxy {

    boolean setRootSources(RootSource... sources);

    ViewNodeInfo getRootNodeInfo();

    boolean enableRootCache();

    boolean disableRootCache();

    ViewInfo[] searchUI(ViewCondition condition);

    ViewInfo[] searchUI(ViewCondition condition, ViewNodeInfo root);

    ViewInfo[] searchUI(ViewCondition condition, ViewInfo viewInfo);

    ViewInfo fetchFirstView(ViewCondition condition);

    ViewInfo fetchFirstView(ViewCondition condition, ViewInfo viewInfo);

    boolean containView(ViewCondition condition);

    boolean containAllViews(ViewCondition... conditions);

    String[] rootViewNames();

    String nowActivity();

    ViewInfo[] waitUntilAppear(ViewCondition condition);

    boolean globalAction(int actionCode);

    boolean gestureTap(int x, int y, int duration);

    boolean gestureSwipe(int x1, int y1, int x2, int y2, int duration);

    boolean tapByResId(String resId);

    boolean tapExByResId(String resId, TapType... tapTypes);

    boolean setTextByResId(String resId, String text);

    boolean focusByResId(String resId);

    boolean tapByText(String text);

    boolean tapExByText(String text, TapType... tapTypes);

    boolean setTextByText(String textTarget, String textInput);

    boolean focusByText(String text);

    boolean waitTapByResId(String resId);

    boolean waitTapExByResId(String resId, TapType... tapTypes);

    boolean waitSetTextByResId(String resId, String text);

    boolean waitFocusByResId(String resId);

    boolean waitTapByText(String text);

    boolean waitTapExByText(String text, TapType... tapTypes);

    boolean waitSetTextByText(String textTarget, String textInput);

    boolean waitFocusByText(String text);

    boolean tap(ViewInfo viewInfo);

    boolean tapEx(ViewInfo viewInfo, TapType... tapTypes);

    boolean setText(ViewInfo viewInfo, String text);

    boolean focus(ViewInfo viewInfo);

    boolean scrollTo(ViewInfo viewInfo, int row, int column);

    boolean tryTap(ViewCondition condition);

    boolean tryTapEx(ViewCondition condition, TapType... tapTypes);

    boolean trySetText(ViewCondition condition, String text);

    boolean tryFocus(ViewCondition condition);

    boolean tryScrollTo(ViewCondition condition, int row, int column);

    ViewInfo waitUi(ViewCondition condition, int timeout);

    ViewInfo waitUi(ViewCondition condition);

    boolean waitActivity(String activity);

    boolean waitActivityAndUi(String activity, ViewCondition condition);

    String waitActivities(String... activities);

    ViewInfo waitUis(ViewCondition... conditions);

    boolean tapView(ViewInfo viewInfo);

    boolean tapViewByGesture(ViewInfo viewInfo);

    boolean tapViewByParent(ViewInfo viewInfo);

    boolean tryTapView(ViewInfo viewInfo);

    boolean tapViewEx(ViewInfo viewInfo, TapType... tapTypes);

    boolean waitTryTap(ViewCondition condition);

    boolean waitExTryTap(ViewCondition condition, TapType... tapTypes);

    boolean setTextByClipboard(ViewInfo viewInfo, String text);

    boolean waitTrySetText(ViewCondition condition, String text);

    boolean waitTryFocus(ViewCondition condition);

    boolean waitTryScrollTo(ViewCondition condition, int row, int column);

    boolean killApp(String pkgName);

    boolean startApp(String pkgName);

}
