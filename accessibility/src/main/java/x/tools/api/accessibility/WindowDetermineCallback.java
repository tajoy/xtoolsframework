package x.tools.api.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

public interface WindowDetermineCallback {
    AccessibilityNodeInfo determine(AccessibilityWindowInfo windowInfo);
}
