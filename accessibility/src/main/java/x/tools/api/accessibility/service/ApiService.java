package x.tools.api.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import x.tools.api.accessibility.RootAccessibilityNodeInfoGetter;
import x.tools.api.accessibility.RootSource;
import x.tools.api.accessibility.TapType;
import x.tools.api.accessibility.WindowDetermineCallback;
import x.tools.api.accessibility.view.ViewCondition;
import x.tools.api.accessibility.view.ViewInfo;
import x.tools.api.accessibility.view.ViewNodeInfo;
import x.tools.eventbus.rpc.RpcFactory;
import x.tools.log.Loggable;

import static android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
import static android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
import static android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
import static android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
import static android.view.accessibility.AccessibilityEvent.TYPES_ALL_MASK;
import static x.tools.api.accessibility.TapType.Click;
import static x.tools.api.accessibility.TapType.Gesture;
import static x.tools.api.accessibility.TapType.LongClick;
import static x.tools.api.accessibility.TapType.ParentClick;
import static x.tools.api.accessibility.TapType.ParentLongClick;
import static x.tools.api.accessibility.view.ViewCondition.TextEqual;

public class ApiService extends AccessibilityService implements Loggable, RootAccessibilityNodeInfoGetter, IApiServiceProxy {
    final static String TAG = ApiService.class.getSimpleName();

    static final int WAIT_PERIOD = 500;
    static final int WAIT_TIMEOUT = 60_000;

    final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableHtmlEscaping()
            .create();

    private ActivityInfo nowActivityInfo;
    private ComponentName nowComponentName;
    private AccessibilityNodeInfo currentWindow = null;

    private void delay(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RpcFactory.registerProxyHost(IApiServiceProxy.class, this, this.getClass().getName());
    }

    @Override
    public void onDestroy() {
        RpcFactory.unregisterProxyHost(IApiServiceProxy.class, this, this.getClass().getName());
        super.onDestroy();
    }

    private String pkgName = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                CharSequence pkgName = event.getPackageName();
                CharSequence clsName = event.getClassName();
                this.pkgName = pkgName == null ? "" : pkgName.toString();
                AccessibilityNodeInfo noteInfo = event.getSource();
                if (noteInfo != null) {
                    if (currentWindow != null) {
                        try {
                            currentWindow.recycle();
                        } catch (Throwable t) {
                        }
                        currentWindow = null;
                    }
                    currentWindow = noteInfo;
                }
                cacheRootViewNodeInfo.set(null);
                cacheSubRootViewNodeInfo.clear();
                if (pkgName != null && clsName != null) {
                    nowComponentName = new ComponentName(
                            pkgName.toString(),
                            clsName.toString()
                    );
                    nowActivityInfo = tryGetActivity(nowComponentName);
                } else {
                    nowComponentName = new ComponentName("", "");
                    nowActivityInfo = null;
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    private final CopyOnWriteArraySet<RootSource> rootSources = new CopyOnWriteArraySet<>(
            Arrays.asList(new RootSource[]{
                    RootSource.DIRECT,
                    RootSource.EVENT,
                    RootSource.WINDOWS
            })
    );

    @Override
    public boolean setRootSources(RootSource... sources) {
        if (sources == null || sources.length <= 0)
            return false;
        rootSources.clear();
        rootSources.addAll(Arrays.asList(sources));
        return true;
    }

    public boolean setRootSources(List<RootSource> sources) {
        if (sources == null || sources.size() <= 0)
            return false;
        rootSources.clear();
        rootSources.addAll(sources);
        return true;
    }

    public AccessibilityNodeInfo getRootAccessibilityNodeInfo(RootSource source) {
        switch (source) {
            case DIRECT:
                return getRootInActiveWindow();
            case EVENT:
                if (currentWindow != null) {
                    if (!currentWindow.refresh()) {
                        try {
                            currentWindow.recycle();
                        } catch (Throwable t) {
                        }
                        currentWindow = null;
                    }
                }
                return currentWindow;
            case WINDOWS: {
                List<AccessibilityWindowInfo> windows = getWindows();
                if (windows == null || windows.size() <= 0) {
                    return null;
                }
                AccessibilityNodeInfo root = null;
                for (AccessibilityWindowInfo window : windows) {
                    WindowDetermineCallback callback = source.getWindowDetermineCallback();
                    if (callback != null && (root = callback.determine(window)) != null) {
                        break;
                    }
                    root = window.getRoot();
                    if (root == null)
                        continue;
                    if (!root.refresh()) {
                        try {
                            root.recycle();
                        } catch (Throwable t) {
                        }
                        continue;
                    }
                    if (window.isActive()) {
                        break;
                    }
                    if (window.isFocused()) {
                        break;
                    }
                    if (window.isAccessibilityFocused()) {
                        break;
                    }
                    try {
                        root.recycle();
                    } catch (Throwable t) {
                    }
                    root = null;
                }
                return root;
            }
        }
        return null;
    }


    @Override
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo() {
        if (rootSources.size() <= 0)
            return null;
        RootSource[] sources = new RootSource[rootSources.size()];
        sources = rootSources.toArray(sources);
        AccessibilityNodeInfo root = null;
        for (RootSource source : sources) {
            if ((root = getRootAccessibilityNodeInfo(source)) != null) {
                break;
            }
        }
        return root;
    }

    private boolean showPackageDetail(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        PackageManager pkgMgr = getPackageManager();
        ApplicationInfo info;
        String appName;
        try {
            info = pkgMgr.getApplicationInfo(packageName, 0);
            appName = pkgMgr.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        for (int i = 0; i < 3; i++) {

            startActivity(intent);

            if (waitUi(TextEqual("应用程序信息"), 5_000) == null) {
                if (!performGlobalAction(GLOBAL_ACTION_BACK)) {
                    return false;
                }
                continue;
            }

            if (waitUi(TextEqual(appName), 5_000) == null) {
                if (!performGlobalAction(GLOBAL_ACTION_BACK)) {
                    return false;
                }
                continue;
            }

            return true;
        }

        return false;
    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.packageNames = null; // all
        info.eventTypes = TYPES_ALL_MASK;
        info.flags = FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                | FLAG_REPORT_VIEW_IDS
                | FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        ;
        setServiceInfo(info);
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private DisplayMetrics displayMetrics;

    public int getWindowWidth() {
        if (displayMetrics == null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        return displayMetrics.widthPixels;
    }

    public int getWindowHeight() {
        if (displayMetrics == null) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        return displayMetrics.heightPixels;
    }

    private final ConcurrentHashMap<String, ViewNodeInfo> cacheSubRootViewNodeInfo = new ConcurrentHashMap<>();
    private final AtomicReference<ViewNodeInfo> cacheRootViewNodeInfo = new AtomicReference<>(null);
    private final AtomicBoolean enableCache = new AtomicBoolean(true);

    @Override
    public ViewNodeInfo getRootNodeInfo() {
        ViewNodeInfo root;
        if (enableCache.get()) {
            root = cacheRootViewNodeInfo.get();
            if (root != null) {
                return root;
            }
        }
        AccessibilityNodeInfo nodeInfo = getRootAccessibilityNodeInfo();
        if (nodeInfo == null)
            return null;
        root = new ViewNodeInfo(nodeInfo, this);
        if (enableCache.get()) {
            cacheRootViewNodeInfo.set(root);
        }
        return root;
    }

    @Override
    public boolean enableRootCache() {
        enableCache.set(true);
        return true;
    }

    @Override
    public boolean disableRootCache() {
        enableCache.set(false);
        cacheRootViewNodeInfo.set(null);
        return true;
    }

    public interface ForeachViewCallback {
        void visit(ViewNodeInfo node, boolean isRoot);
    }

    private void foreachView(ViewNodeInfo node, ForeachViewCallback callback, boolean isRoot) {
        callback.visit(node, isRoot);
        if (node.children != null && node.children.size() > 0) {
            for (ViewNodeInfo childNode : node.children) {
                foreachView(childNode, callback, false);
            }
        }
    }

    public boolean foreachView(ForeachViewCallback callback, ViewNodeInfo root) {
        if (root == null)
            return false;
        foreachView(root, callback, true);
        return true;
    }

    public boolean foreachView(ForeachViewCallback callback) {
        ViewNodeInfo root = getRootNodeInfo();
        if (root == null)
            return false;
        foreachView(root, callback, true);
        return true;
    }

    @Override
    public ViewInfo[] searchUI(ViewCondition condition) {
        if (condition == null)
            return new ViewInfo[0];
        List<ViewInfo> ret = new ArrayList<>();
        foreachView((node, isRoot) -> {
            if (condition.judge(node)) {
                ret.add(node.info);
            }
        });
        return ret.toArray(new ViewInfo[ret.size()]);
    }

    @Override
    public ViewInfo[] searchUI(ViewCondition condition, ViewNodeInfo root) {
        if (condition == null || root == null)
            return new ViewInfo[0];
        List<ViewInfo> ret = new ArrayList<>();
        foreachView((node, isRoot) -> {
            if (condition.judge(node)) {
                ret.add(node.info);
            }
        }, root);
        return ret.toArray(new ViewInfo[ret.size()]);
    }

    @Override
    public ViewInfo[] searchUI(ViewCondition condition, ViewInfo viewInfo) {
        if (condition == null || viewInfo == null)
            return new ViewInfo[0];
        List<ViewInfo> ret = new ArrayList<>();
        if (viewInfo.vid != null) {
            ViewNodeInfo rootNode = cacheSubRootViewNodeInfo.get(viewInfo.vid);
            if (rootNode != null) {
                foreachView((node, isRoot) -> {
                    if (condition.judge(node)) {
                        ret.add(node.info);
                    }
                }, rootNode);
                return ret.toArray(new ViewInfo[ret.size()]);
            }
        }
        viewInfo.getAccessibilityNodeInfo((root) -> {
            ViewNodeInfo rootNode = new ViewNodeInfo(root, () -> root);
            if (enableCache.get() && viewInfo.vid != null) {
                cacheSubRootViewNodeInfo.put(viewInfo.vid, rootNode);
            }
            foreachView((node, isRoot) -> {
                if (condition.judge(node)) {
                    ret.add(node.info);
                }
            }, rootNode);
        });
        return ret.toArray(new ViewInfo[ret.size()]);
    }

    private interface _TryCallback {
        Object call(AccessibilityNodeInfo nodeInfo) throws Throwable;
    }

    private boolean _try(_TryCallback callback, ViewCondition condition) {
        ViewInfo[] views = this.searchUI(condition);
        if (views == null || views.length <= 0) {
            // log.d("Utility._try", String.format("condition: %s", condition));
            return false;
        }


        for (ViewInfo node : views) {
            AtomicBoolean isThrow = new AtomicBoolean(false);
            node.getAccessibilityNodeInfo((nodeInfo) -> {
                if (nodeInfo != null) {
                    try {
                        callback.call(nodeInfo);
                    } catch (Throwable throwable) {
                        isThrow.set(true);
                    }
                }
            });
            if (isThrow.get()) {
                continue;
            }
            return true;
        }


        // log.d("Utility._try", String.format("condition: %s -> result.views: %s", condition, result.views.toString()));
        return false;
    }

    private boolean _try(_TryCallback callback, List<ViewInfo> views) {
        if (views == null || views.size() <= 0) {
            // log.d("Utility._try", String.format("views: %s", views));
            return false;
        }

        for (ViewInfo node : views) {
            AtomicBoolean isThrow = new AtomicBoolean(false);
            node.getAccessibilityNodeInfo((nodeInfo) -> {
                if (nodeInfo != null) {
                    try {
                        callback.call(nodeInfo);
                    } catch (Throwable throwable) {
                        isThrow.set(true);
                    }
                }
            });
            if (isThrow.get()) {
                continue;
            }
            return true;
        }

        // log.d("Utility._try", String.format("views: %s", views));
        return false;
    }


    private boolean _tryA(_TryCallback callback, List<AccessibilityNodeInfo> nodes) {
        if (nodes == null || nodes.size() <= 0) {
            // log.d("Utility._try", String.format("views: %s", views));
            return false;
        }
        for (AccessibilityNodeInfo nodeInfo : nodes) {
            try {
                callback.call(nodeInfo);
            } catch (Throwable throwable) {
                continue;
            }
            return true;
        }
        // log.d("Utility._try", String.format("views: %s", views));
        return false;
    }

    private interface _TryRetCallback<T> {
        T call(AccessibilityNodeInfo nodeInfo) throws Throwable;
    }

    private <T> T _tryRet(_TryRetCallback<T> callback, ViewCondition condition) {
        ViewInfo[] views = this.searchUI(condition);
        if (views == null || views.length <= 0) {
            return null;
        }

        for (ViewInfo node : views) {
            AtomicBoolean isThrow = new AtomicBoolean(false);
            T ret = node.getAccessibilityNodeInfoRet((nodeInfo) -> {
                if (nodeInfo != null) {
                    try {
                        return callback.call(nodeInfo);
                    } catch (Throwable throwable) {
                        isThrow.set(true);
                        return null;
                    }
                }
                return null;
            });
            if (isThrow.get()) {
                continue;
            }
            return ret;
        }
        return null;
    }

    private <T> T _tryRet(_TryRetCallback<T> callback, List<ViewInfo> views) {
        if (views == null || views.size() <= 0) {
            return null;
        }

        for (ViewInfo node : views) {
            AtomicBoolean isThrow = new AtomicBoolean(false);
            T ret = node.getAccessibilityNodeInfoRet((nodeInfo) -> {
                if (nodeInfo != null) {
                    try {
                        return callback.call(nodeInfo);
                    } catch (Throwable throwable) {
                        isThrow.set(true);
                        return null;
                    }
                }
                return null;
            });
            if (isThrow.get()) {
                continue;
            }
            return ret;
        }
        return null;
    }


    private <T> T _tryARet(_TryRetCallback<T> callback, List<AccessibilityNodeInfo> nodes) {
        if (nodes == null || nodes.size() <= 0) {
            return null;
        }

        for (AccessibilityNodeInfo nodeInfo : nodes) {
            try {
                T ret = callback.call(nodeInfo);
                if (ret != null) {
                    return ret;
                }
            } catch (Throwable throwable) {
                continue;
            }
        }
        return null;
    }

    interface _WaitUntilAppearCallback {
        boolean call(List<ViewInfo> views);
    }

    interface _WaitUntilAppearCallbackRet<T> {
        T call(List<ViewInfo> views);
    }

    private boolean _waitUntilAppear(ViewCondition condition, _WaitUntilAppearCallback callback, int wait_period, int timeout) {
        ViewInfo[] views = waitUntil(() -> {
            ViewInfo[] ret = this.searchUI(condition);
            if (ret != null && ret.length > 0) {
                // log.d("_waitUntilAppear", String.format("condition: %s -> result.views: %s", condition, result.views.toString()));
                return ret;
            }
            return null;
        }, wait_period, timeout);
        if (views == null || views.length <= 0) {
            return false;
        }
        return callback.call(Arrays.asList(views));
    }

    private <T> T _waitUntilAppear(ViewCondition condition, _WaitUntilAppearCallbackRet<T> callback, int wait_period, int timeout) {
        ViewInfo[] views = waitUntil(() -> {
            ViewInfo[] ret = this.searchUI(condition);
            if (ret != null && ret.length > 0) {
                // log.d("_waitUntilAppear", String.format("condition: %s -> result.views: %s", condition, result.views.toString()));
                return ret;
            }
            return null;
        }, wait_period, timeout);
        if (views == null || views.length <= 0) {
            return null;
        }
        return callback.call(Arrays.asList(views));
    }

    public interface WaitUntilCallback {
        boolean call();
    }

    public boolean waitUntil(WaitUntilCallback callback, int wait_period, int timeout) {
        long time_begin = new Date().getTime();
        WaitUntilCallback exceptionWrapper = () -> {
            try {
                return callback.call();
            } catch (Throwable throwable) {
                return false;
            }
        };
        while (!exceptionWrapper.call()) {
            long now = new Date().getTime();
            if (now > time_begin + timeout) {
                // log.d("Utility.waitUntil", String.format("now: %d, begin: %d", now, time_begin));
                return false;
            }
            this.delay(wait_period);
        }

        return true;
    }

    public boolean waitUntil(WaitUntilCallback callback, int timeout) {
        return waitUntil(callback, WAIT_PERIOD, timeout);
    }

    public boolean waitUntil(WaitUntilCallback callback) {
        return waitUntil(callback, WAIT_TIMEOUT);
    }

    public interface WaitUntilCallbackRet<T> {
        T call();
    }

    public <T> T waitUntil(WaitUntilCallbackRet<T> callback, int wait_period, int timeout) {
        long time_begin = new Date().getTime();
        WaitUntilCallbackRet<T> exceptionWrapper = () -> {
            try {
                return callback.call();
            } catch (Throwable throwable) {
                return null;
            }
        };
        T ret = exceptionWrapper.call();
        while (ret == null) {
            long now = new Date().getTime();
            if (now > time_begin + timeout) {
                // log.d("Utility.waitUntil", String.format("now: %d, begin: %d", now, time_begin));
                return null;
            }
            this.delay(wait_period);
            ret = exceptionWrapper.call();
        }

        return ret;
    }

    public <T> T waitUntil(WaitUntilCallbackRet<T> callback, int timeout) {
        return waitUntil(callback, WAIT_PERIOD, timeout);
    }

    public <T> T waitUntil(WaitUntilCallbackRet<T> callback) {
        return waitUntil(callback, WAIT_TIMEOUT);
    }


    @Override
    public ViewInfo fetchFirstView(ViewCondition condition) {
        ViewInfo[] views = this.searchUI(condition);
        if (views != null && views.length > 0) {
            return views[0];
        }
        return null;
    }

    @Override
    public ViewInfo fetchFirstView(ViewCondition condition, ViewInfo viewInfo) {
        ViewInfo[] views = this.searchUI(condition, viewInfo);
        if (views != null && views.length > 0) {
            return views[0];
        }
        return null;
    }

    @Override
    public boolean containView(ViewCondition condition) {
        return searchUI(condition).length > 0;
    }

    public boolean containAllViews(List<ViewCondition> conditions) {
        for (ViewCondition condition : conditions) {
            if (searchUI(condition).length <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containAllViews(ViewCondition... conditions) {
        return containAllViews(Arrays.asList(conditions));
    }

    @Override
    public String[] rootViewNames() {
        List<AccessibilityWindowInfo> windowInfoList = getWindows();
        if (windowInfoList == null || windowInfoList.size() <= 0)
            return new String[0];
        List<String> names = new ArrayList<>();
        for (AccessibilityWindowInfo window : windowInfoList) {
            CharSequence title = window.getTitle();
            if (title != null)
                names.add(title.toString());
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public String nowActivity() {
        List<AccessibilityWindowInfo> windowInfoList = getWindows();
        if (windowInfoList == null || windowInfoList.size() <= 0)
            return "";
        AccessibilityWindowInfo topWindow = windowInfoList.get(0);
        CharSequence title = topWindow.getTitle();
        return title == null ? "" : title.toString();
    }

    @Override
    public ViewInfo[] waitUntilAppear(ViewCondition condition) {
        return _waitUntilAppear(condition, (_WaitUntilAppearCallbackRet<ViewInfo[]>) (views) -> {
            if (views != null) {
                return views.toArray(new ViewInfo[views.size()]);
            }
            return (ViewInfo[]) null;
        }, WAIT_PERIOD, WAIT_TIMEOUT);
    }

    @Override
    public boolean globalAction(int actionCode) {
        if (actionCode < 1 || actionCode > 7) {
            error("expect action code >= 1 and <= 7, but got %s", actionCode);
            return false;
        }
        return performGlobalAction(actionCode);
    }

    @Override
    public boolean gestureTap(int x, int y, int duration) {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x, y);
        //path.lineTo(x+2, y+2);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, duration));
        return dispatchGesture(gestureBuilder.build(), null, null);
    }

    public boolean gestureTap(int x, int y) {
        return gestureTap(x, y, 50);
    }

    @Override
    public boolean gestureSwipe(int x1, int y1, int x2, int y2, int duration) {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, duration));
        return dispatchGesture(gestureBuilder.build(), null, null);
    }


    public boolean gestureSwipe(int x1, int y1, int x2, int y2) {
        return gestureSwipe(x1, y1, x2, y2, 500);
    }


    @Override
    public boolean tapByResId(String resId) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> this.tryTapView(nodeInfo), nodes);
    }

    @Override
    public boolean tapExByResId(String resId, TapType... tapTypes) {
        return tapExByResId(resId, Arrays.asList(tapTypes));
    }

    public boolean tapExByResId(String resId, List<TapType> tapTypes) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> this.tapViewEx(nodeInfo, tapTypes.toArray(new TapType[tapTypes.size()])), nodes);
    }

    @Override
    public boolean setTextByResId(String resId, String text) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> this.setText(nodeInfo, text), nodes);
    }

    @Override
    public boolean focusByResId(String resId) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS), nodes);
    }


    @Override
    public boolean tapByText(String text) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> this.tryTapView(nodeInfo), nodes);
    }

    @Override
    public boolean tapExByText(String text, TapType... tapTypes) {
        return tapExByText(text, Arrays.asList(tapTypes));
    }

    public boolean tapExByText(String text, List<TapType> tapTypes) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> this.tapViewEx(nodeInfo, tapTypes.toArray(new TapType[tapTypes.size()])), nodes);
    }

    @Override
    public boolean setTextByText(String textTarget, String textInput) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(textTarget);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> this.setText(nodeInfo, textInput), nodes);
    }

    @Override
    public boolean focusByText(String text) {
        AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
        if (root == null)
            return false;
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(text);
        if (nodes == null || nodes.size() < 0) {
            return false;
        }
        return this._tryA(nodeInfo -> nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS), nodes);
    }

    @Override
    public boolean waitTapByResId(String resId) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> this.tryTapView(nodeInfo), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean waitTapExByResId(String resId, TapType... tapTypes) {
        return waitTapExByResId(resId, Arrays.asList(tapTypes));
    }

    public boolean waitTapExByResId(String resId, List<TapType> tapTypes) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> this.tapViewEx(nodeInfo, tapTypes.toArray(new TapType[tapTypes.size()])), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean waitSetTextByResId(String resId, String text) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> this.setText(nodeInfo, text), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean waitFocusByResId(String resId) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(resId);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }


    @Override
    public boolean waitTapByText(String text) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> this.tryTapView(nodeInfo), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean waitTapExByText(String text, TapType... tapTypes) {
        return waitTapExByText(text, Arrays.asList(tapTypes));
    }


    public boolean waitTapExByText(String text, List<TapType> tapTypes) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> this.tapViewEx(nodeInfo, tapTypes.toArray(new TapType[tapTypes.size()])), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean waitSetTextByText(String textTarget, String textInput) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> this.setText(nodeInfo, textInput), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(textTarget);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean waitFocusByText(String text) {
        return this.waitUntil(() -> this._tryA(nodeInfo -> nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS), this.waitUntil(() -> {
            AccessibilityNodeInfo root = getRootAccessibilityNodeInfo();
            if (root == null)
                return null;
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
            if (nodes == null || nodes.size() < 0) {
                return null;
            }
            return nodes;
        })));
    }

    @Override
    public boolean tap(ViewInfo viewInfo) {
        if (viewInfo == null)
            return false;
        return this.tryTapView(viewInfo);
    }

    @Override
    public boolean tapEx(ViewInfo viewInfo, TapType... tapTypes) {
        return tapEx(viewInfo, Arrays.asList(tapTypes));
    }

    public boolean tapEx(ViewInfo viewInfo, List<TapType> tapTypes) {
        if (viewInfo == null)
            return false;
        return this.tapViewEx(viewInfo, tapTypes);
    }

    @Override
    public boolean setText(ViewInfo viewInfo, String text) {
        if (viewInfo == null)
            return false;
        return viewInfo.getAccessibilityNodeInfoRet(nodeInfo -> setText(nodeInfo, text));
    }

    @Override
    public boolean focus(ViewInfo viewInfo) {
        if (viewInfo == null)
            return false;
        return viewInfo.getAccessibilityNodeInfoRet((n) -> n.performAction(AccessibilityNodeInfo.ACTION_FOCUS));
    }

    @Override
    public boolean scrollTo(ViewInfo viewInfo, int row, int column) {
        if (viewInfo == null)
            return false;
        return viewInfo.getAccessibilityNodeInfoRet((n) -> {
            Bundle bundle = new Bundle();
            bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, row);
            bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_COLUMN_INT, column);
            return n.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.getId(), bundle);
        });
    }

    @Override
    public boolean tryTap(ViewCondition condition) {
        return this._try((nodeInfo) -> this.tryTapView(nodeInfo), condition);
    }

    @Override
    public boolean tryTapEx(ViewCondition condition, TapType... tapTypes) {
        return tryTapEx(condition, Arrays.asList(tapTypes));
    }

    public boolean tryTapEx(ViewCondition condition, List<TapType> tapTypes) {
        return this._try((nodeInfo) -> this.tapViewEx(nodeInfo, tapTypes.toArray(new TapType[tapTypes.size()])), condition);
    }

    public boolean trySetText(AccessibilityNodeInfo nodeInfo, String text) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        if (!nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)) {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            boolean isSet = true;
            do {
                ClipData clip = ClipData.newPlainText("text", text);
                clipboard.setPrimaryClip(clip);
                isSet = true;
                int itemCount = clipboard.getPrimaryClip().getItemCount();
                if (itemCount < 1) {
                    isSet = false;
                }
                boolean isContain = false;
                for (int i = 0; i < itemCount; i++) {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(i);
                    if (text.equals(item.getText())) {
                        isContain = true;
                        break;
                    }
                }
                if (!isContain)
                    isSet = false;
            } while (!isSet);
            if (!nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS))
                return false;
            if (!nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE))
                return false;
        }
        return true;
    }

    @Override
    public boolean trySetText(ViewCondition condition, String text) {
        return this._try(nodeInfo -> this.trySetText(nodeInfo, text), condition);
    }

    @Override
    public boolean tryFocus(ViewCondition condition) {
        return this._try((nodeInfo) -> nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS), condition);
    }

    @Override
    public boolean tryScrollTo(ViewCondition condition, int row, int column) {
        return this._try((n) -> {
            Bundle bundle = new Bundle();
            bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, row);
            bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_COLUMN_INT, column);
            return n.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.getId(), bundle);
        }, condition);
    }

    @Override
    public ViewInfo waitUi(ViewCondition condition, int timeout) {
        if (condition == null)
            return null;
        return waitUntil(() -> {
            ViewInfo[] views = this.searchUI(condition);
            if (views != null && views.length > 0) {
                return views[0];
            }
            return null;
        }, WAIT_PERIOD, timeout);
    }

    @Override
    public ViewInfo waitUi(ViewCondition condition) {
        if (condition == null)
            return null;
        return waitUi(condition, WAIT_TIMEOUT);
    }

    @Override
    public boolean waitActivity(String activity) {
        if (activity == null)
            return false;
        return waitUntil(() -> nowComponentName != null && activity.equals(nowComponentName.getClassName()), WAIT_PERIOD, WAIT_TIMEOUT);
    }

    @Override
    public boolean waitActivityAndUi(String activity, ViewCondition condition) {
        if (activity == null)
            return false;
        if (condition == null)
            return false;
        return waitUntil(() -> {
            if (nowComponentName == null)
                return false;
            if (!activity.equals(nowComponentName.getClassName()))
                return false;
            if (!this.containView(condition))
                return false;
            return true;
        }, WAIT_PERIOD, WAIT_TIMEOUT);
    }

    @Override
    public String waitActivities(String ... activities) {
        if (activities == null || activities.length <= 0)
            return null;
        return waitUntil(() -> {
            if (nowComponentName == null)
                return null;
            for (String act : activities) {
                if (!act.equals(nowComponentName.getClassName())) {
                    return act;
                }
            }
            return null;
        }, WAIT_PERIOD, WAIT_TIMEOUT);
    }

    public String waitActivities(List<String> activities) {
        if (activities == null || activities.size() <= 0)
            return null;
        return waitUntil(() -> {
            if (nowComponentName == null)
                return null;
            for (String act : activities) {
                if (!act.equals(nowComponentName.getClassName())) {
                    return act;
                }
            }
            return null;
        }, WAIT_PERIOD, WAIT_TIMEOUT);
    }

    public ViewInfo waitUis(List<ViewCondition> conditions) {
        if (conditions == null || conditions.size() <= 0)
            return null;
        return waitUntil(() -> {
            for (ViewCondition condition : conditions) {
                ViewInfo viewInfo = this.fetchFirstView(condition);
                if (viewInfo != null) {
                    return viewInfo;
                }
            }
            return null;
        }, WAIT_PERIOD, WAIT_TIMEOUT);
    }

    @Override
    public ViewInfo waitUis(ViewCondition... conditions) {
        return waitUis(Arrays.asList(conditions));
    }

    public boolean tapView(AccessibilityNodeInfo nodeInfo) {
        return tapViewEx(nodeInfo, Click(), LongClick());
    }

    @Override
    public boolean tapView(ViewInfo viewInfo) {
        return tapViewEx(viewInfo, Click(), LongClick());
    }

    public boolean tapViewByGesture(AccessibilityNodeInfo nodeInfo) {
        return tapViewEx(nodeInfo, Gesture());
    }

    @Override
    public boolean tapViewByGesture(ViewInfo viewInfo) {
        return tapViewEx(viewInfo, Gesture());
    }

    public boolean tapViewByParent(AccessibilityNodeInfo nodeInfo) {
        return tapViewEx(nodeInfo, ParentClick(), ParentLongClick());
    }

    @Override
    public boolean tapViewByParent(ViewInfo viewInfo) {
        return tapViewEx(viewInfo, ParentClick(), ParentLongClick());
    }

    public boolean tryTapView(AccessibilityNodeInfo nodeInfo) {
        return tapViewEx(nodeInfo, Click(), LongClick(), ParentClick(), ParentLongClick(), Gesture());
    }

    @Override
    public boolean tryTapView(ViewInfo viewInfo) {
        return tapViewEx(viewInfo, Click(), LongClick(), ParentClick(), ParentLongClick(), Gesture());
    }

    public boolean tapViewEx(AccessibilityNodeInfo nodeInfo, TapType... tapTypes) {
        return tapViewEx(nodeInfo, Arrays.asList(tapTypes));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }


    public boolean tapViewEx(AccessibilityNodeInfo nodeInfo, List<TapType> tapTypes) {
        if (nodeInfo == null)
            return false;

        for (TapType tapType : tapTypes) {
            switch (tapType.getType()) {
                case CLICK:
                    if (nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                        return true;
                    break;
                case LONG_CLICK:
                    if (nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK))
                        return true;
                    break;
                case PARENT_CLICK: {
                    int parentLevel = tapType.getParentLevel();
                    AccessibilityNodeInfo parent = nodeInfo.getParent();
                    for (int i = 0; parent != null && (parentLevel == 0 || i < parentLevel); i++) {
                        if (parent != null) {
                            if (parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                return true;
                            }
                        }
                        parent = parent.getParent();
                        if (parentLevel == 0 && parent.getChildCount() != 1)
                            break;
                    }
                    break;
                }
                case PARENT_LONG_CLICK: {
                    int parentLevel = tapType.getParentLevel();
                    AccessibilityNodeInfo parent = nodeInfo.getParent();
                    for (int i = 0; parent != null && (parentLevel == 0 || i < parentLevel); i++) {
                        if (parent != null) {
                            if (parent.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)) {
                                return true;
                            }
                        }
                        parent = parent.getParent();
                        if (parentLevel == 0 && parent.getChildCount() != 1)
                            break;
                    }
                    break;
                }
                case GESTURE: {
                    Rect rect = new Rect();
                    nodeInfo.getBoundsInScreen(rect);
                    GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                    Path path = new Path();
                    int cx = clamp(rect.centerX(), 0, getWindowWidth());
                    int cy = clamp(rect.centerY(), 0, getWindowHeight());
                    path.moveTo(cx, cy);
                    gestureBuilder.addStroke(
                            new GestureDescription.StrokeDescription(
                                    path,
                                    tapType.getStartTime(),
                                    tapType.getDuration()
                            )
                    );
                    if (dispatchGesture(gestureBuilder.build(), null, null)) {
                        return true;
                    }
                    break;
                }
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public boolean tapViewEx(ViewInfo viewInfo, TapType... tapTypes) {
        return tapViewEx(viewInfo, Arrays.asList(tapTypes));
    }

    public boolean tapViewEx(ViewInfo viewInfo, List<TapType> tapTypes) {
        if (viewInfo == null)
            return false;
        return viewInfo.getAccessibilityNodeInfoRet((nodeInfo) -> tapViewEx(nodeInfo, tapTypes));
    }


    @Override
    public boolean waitTryTap(ViewCondition condition) {
        if (condition == null)
            return false;
        _WaitUntilAppearCallback callback = (views) -> this._try((nodeInfo) -> this.tryTapView(nodeInfo), views);
        return this._waitUntilAppear(
                condition,
                callback,
                WAIT_PERIOD,
                WAIT_TIMEOUT
        );
    }

    @Override
    public boolean waitExTryTap(ViewCondition condition, TapType... tapTypes) {
        return waitExTryTap(condition, Arrays.asList(tapTypes));
    }

    public boolean waitExTryTap(ViewCondition condition, List<TapType> tapTypes) {
        if (condition == null)
            return false;
        _WaitUntilAppearCallback callback = (views) -> this._try((nodeInfo) -> this.tapViewEx(nodeInfo, tapTypes), views);
        return this._waitUntilAppear(
                condition,
                callback,
                WAIT_PERIOD,
                WAIT_TIMEOUT
        );
    }

    public boolean setText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null)
            return false;
        Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);
    }

    public boolean setTextByClipboard(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null)
            return false;
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        boolean isSet = true;
        do {
            ClipData clip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clip);
            isSet = true;
            int itemCount = clipboard.getPrimaryClip().getItemCount();
            if (itemCount < 1) {
                isSet = false;
            }
            boolean isContain = false;
            for (int i = 0; i < itemCount; i++) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(i);
                if (text.equals(item.getText())) {
                    isContain = true;
                    break;
                }
            }
            if (!isContain)
                isSet = false;
        } while (!isSet);
        if (!nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                && !nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                && !nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK))
            return false;
        if (!nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE))
            return false;
        return true;

    }

    @Override
    public boolean setTextByClipboard(ViewInfo viewInfo, String text) {
        if (viewInfo == null)
            return false;
        return viewInfo.getAccessibilityNodeInfoRet(nodeInfo -> setTextByClipboard(nodeInfo, text));
    }

    @Override
    public boolean waitTrySetText(ViewCondition condition, String text) {
        if (condition == null)
            return false;
        _WaitUntilAppearCallback callback = (views) -> this._try((_TryCallback) (nodeInfo) -> {
            if (!setText(nodeInfo, text)) {
                return setTextByClipboard(nodeInfo, text);
            }
            return true;
        }, views);
        return this._waitUntilAppear(
                condition,
                callback,
                WAIT_PERIOD,
                WAIT_TIMEOUT
        );
    }

    @Override
    public boolean waitTryFocus(ViewCondition condition) {
        if (condition == null)
            return false;
        _WaitUntilAppearCallback callback = (views) -> this._try((nodeInfo) -> nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS), views);
        return this._waitUntilAppear(
                condition,
                callback,
                WAIT_PERIOD,
                WAIT_TIMEOUT
        );
    }

    @Override
    public boolean waitTryScrollTo(ViewCondition condition, int row, int column) {
        if (condition == null)
            return false;
        _WaitUntilAppearCallback callback = (views) -> this._try((n) -> {
            Bundle bundle = new Bundle();
            bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, row);
            bundle.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_COLUMN_INT, column);
            return n.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.getId(), bundle);
        }, views);
        return this._waitUntilAppear(
                condition,
                callback,
                WAIT_PERIOD,
                WAIT_TIMEOUT
        );
    }

    final private ReentrantLock lockKillApp = new ReentrantLock();

    @Override
    public boolean killApp(String pkgName) {
        try {
            lockKillApp.lock();

            if (!showPackageDetail(pkgName))
                return false;

            int count = 0;
            if (tryTap(TextEqual("强制停止"))) {
                count++;
                delay(500);
                if (tryTap(TextEqual("确定"))) {
                    count++;
                    delay(500);
                }
            }

            if (tryTap(TextEqual("强制停止"))) {
                count++;
                delay(500);
            }

            if (tryTap(TextEqual("确定"))) {
                count++;
                delay(500);
            }

            if (count < 2) {
                return false;
            }

            if (!performGlobalAction(GLOBAL_ACTION_BACK)) {
                return false;
            }

            delay(500);

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally {
            lockKillApp.unlock();
        }
        return true;
    }

    @Override
    public boolean startApp(String pkgName) {
        try {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(pkgName);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            error("未知错误: ", e);
            return false;
        }
    }


}
