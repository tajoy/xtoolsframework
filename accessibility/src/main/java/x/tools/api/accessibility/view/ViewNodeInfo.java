package x.tools.api.accessibility.view;


import android.view.accessibility.AccessibilityNodeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import x.tools.api.accessibility.RootAccessibilityNodeInfoGetter;

public class ViewNodeInfo implements Serializable {
    private static final long serialVersionUID = 8403147120405020695L;
    public ViewInfo info = null;
    public List<ViewNodeInfo> children = null;

    public ViewNodeInfo parent = null;

    public ViewNodeInfo(ViewInfo info) {
        this.info = info;
    }

    public ViewNodeInfo(AccessibilityNodeInfo nodeInfo, RootAccessibilityNodeInfoGetter rootNodeGetter) {
        this.info = ViewInfo.create(nodeInfo, rootNodeGetter);

        int childCount = nodeInfo.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNodeInfo;
            try {
                childNodeInfo = nodeInfo.getChild(i);
            } catch (Throwable t) {
                continue;
            }
            if (childNodeInfo != null) {
                if (childNodeInfo.refresh()) {
                    ViewNodeInfo childViewNodeInfo = new ViewNodeInfo(childNodeInfo, rootNodeGetter);
                    childViewNodeInfo.parent = this;
                    this.addChild(childViewNodeInfo);
                }
                try {
                    childNodeInfo.recycle();
                } catch (Throwable t) {}
            }
        }
    }

//    public ViewNodeInfo(ViewInfo info) {
//        this.info = info;
//    }

    public void addChild(ViewNodeInfo child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }

    public ViewNodeInfo newCopyWithoutParent() {
        ViewNodeInfo newOne = new ViewNodeInfo(this.info.newCopy());

        if (this.children != null) {
            for (ViewNodeInfo child : this.children) {
                newOne.addChild(child.newCopyWithoutParent());
            }
        }
        return newOne;
    }

    public void unifyUiTreeParent(boolean isSetParent) {
        Stack<ViewNodeInfo> stack = new Stack<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            ViewNodeInfo parent = stack.pop();
            if (parent.children != null && parent.children.size() > 0) {
                for (ViewNodeInfo itNode : parent.children) {
                    if (isSetParent) {
                        itNode.parent = parent;
                    } else {
                        itNode.parent = null;
                    }
                    stack.push(itNode);
                }
            }
        }
    }


    @Override
    public String toString() {
        return "ViewNodeInfo{" +
                "info=" + info +
                ", children=" + children +
                '}';
    }
}
