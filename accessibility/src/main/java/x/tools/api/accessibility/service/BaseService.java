package x.tools.api.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

import x.tools.api.accessibility.RootAccessibilityNodeInfoGetter;
import x.tools.api.accessibility.RootSource;
import x.tools.api.accessibility.WindowDetermineCallback;
import x.tools.framework.log.Loggable;

public abstract class BaseService extends AccessibilityService implements Loggable, RootAccessibilityNodeInfoGetter {
    public String packageName = "";
    public AccessibilityNodeInfo currentWindow = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        debug("事件---->" + event);
        int type = event.getEventType();
        AccessibilityNodeInfo noteInfo;
        noteInfo = event.getSource();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
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
            }
        }
    }


    @Override
    public void onInterrupt() {
        Toast.makeText(this, "支付宝辅助已经中断", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    private final CopyOnWriteArraySet<RootSource> rootSources = new CopyOnWriteArraySet<>(
            Arrays.asList(new RootSource[]{
                    RootSource.DIRECT,
                    RootSource.EVENT,
                    RootSource.WINDOWS
            })
    );

    public boolean setRootSources(RootSource ...sources) {
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
                        } catch (Throwable t) {}
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
        for (RootSource source: sources) {
            if ((root = getRootAccessibilityNodeInfo(source)) != null) {
                break;
            }
        }
        return root;
    }

        public void delay(int ms) {
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public boolean isBackground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(getApplicationContext().getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    info("后台 %s", appProcess.processName);
                    return true;
                } else {
                    info("前台 %s", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

}
