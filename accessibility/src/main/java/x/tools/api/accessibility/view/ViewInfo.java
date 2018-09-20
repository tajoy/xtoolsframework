package x.tools.api.accessibility.view;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.gson.Gson;

import java.io.Serializable;

import x.tools.api.accessibility.RootAccessibilityNodeInfoGetter;
import x.tools.framework.ReflectUtils;

/**
 * Created by jacky-pro on 2018/3/27.
 */
public class ViewInfo implements Serializable {
    private static final long serialVersionUID = 4588156394217449967L;

    public static final long UNDEFINED_AID = Long.MAX_VALUE;

    public long aid = UNDEFINED_AID;
    public long id = -1;
    public String vid = null;
    public int index = -1;
    public String tag = null;
    public String text = null;
    public String hint = null;
    public String desc = null;
    public String res = null;
    public String cls = null;
    public String pkg = null;
    public Rect drawing_rect = null;
    public Rect bounds_on_screen = null;
    public String background = null;
    public String foreground = null;
    public int pos_in_adapter = -1;
    public String item_data_in_adapter = null;

    public boolean is_visible = false;
    public boolean is_enable = false;
    public boolean is_focusable = false;
    public boolean is_focused = false;
    public boolean is_selected = false;
    public boolean is_clickable = false;
    public boolean is_long_clickable = false;
    public boolean is_context_clickable = false;
    public boolean is_scrollable = false;
    public boolean is_checked = false;
    public boolean is_dismissable = false;
    public boolean is_editable = false;
    public boolean is_multiLine = false;
    public boolean is_password = false;
    public Collection collection = null;
    public CollectionItem collection_item = null;

    public static class Collection {
        public final int row_count;
        public final int column_count;
        public final boolean is_hierarchical;
        public final int selection_mode;
        Collection(AccessibilityNodeInfo.CollectionInfo collectionInfo) {
            row_count = collectionInfo.getRowCount();
            column_count = collectionInfo.getColumnCount();
            is_hierarchical = collectionInfo.isHierarchical();
            selection_mode = collectionInfo.getSelectionMode();
        }
        Collection(Collection collection) {
            row_count = collection.row_count;
            column_count = collection.column_count;
            is_hierarchical = collection.is_hierarchical;
            selection_mode = collection.selection_mode;
        }

        @Override
        public String toString() {
            return "Collection{" +
                    "row_count=" + row_count +
                    ", column_count=" + column_count +
                    ", is_hierarchical=" + is_hierarchical +
                    ", selection_mode=" + selection_mode +
                    '}';
        }
    }
    public static class CollectionItem {
        public final boolean is_heading;
        public final int row_index;
        public final int column_index;
        public final int row_span;
        public final int column_span;
        public final boolean is_selected;
        CollectionItem(AccessibilityNodeInfo.CollectionItemInfo collectionItemInfo) {
            is_heading = collectionItemInfo.isHeading();
            column_index = collectionItemInfo.getColumnIndex();
            row_index = collectionItemInfo.getRowIndex();
            column_span = collectionItemInfo.getColumnSpan();
            row_span = collectionItemInfo.getRowSpan();
            is_selected = collectionItemInfo.isSelected();
        }
        CollectionItem(CollectionItem collectionItem) {
            is_heading = collectionItem.is_heading;
            column_index = collectionItem.column_index;
            row_index = collectionItem.row_index;
            column_span = collectionItem.column_span;
            row_span = collectionItem.row_span;
            is_selected = collectionItem.is_selected;
        }

        @Override
        public String toString() {
            return "CollectionItem{" +
                    "is_heading=" + is_heading +
                    ", column_index=" + column_index +
                    ", row_index=" + row_index +
                    ", column_span=" + column_span +
                    ", row_span=" + row_span +
                    ", is_selected=" + is_selected +
                    '}';
        }
    }

    private RootAccessibilityNodeInfoGetter rootNodeGetter = null;

    private static Gson gson = null;

    private static Gson getGson() {
        if (gson == null) {
            gson = new Gson().newBuilder()
                    .serializeNulls()
                    .disableHtmlEscaping()
                    .create();
        }
        return gson;
    }

    public interface GetAccessibilityNodeInfoCallback {
        void call(AccessibilityNodeInfo nodeInfo);
    }
    public void getAccessibilityNodeInfo(GetAccessibilityNodeInfoCallback callback) {
        if (this.rootNodeGetter == null) {
            callback.call(null);
            return;
        }
        if (this.aid == UNDEFINED_AID) {
            callback.call(null);
            return;
        }
        AccessibilityNodeInfo nodeInfo;
        try {
            AccessibilityNodeInfo root = rootNodeGetter.getRootAccessibilityNodeInfo();
            if (root == null) {
                callback.call(null);
                return;
            }
            nodeInfo = (AccessibilityNodeInfo) ReflectUtils.callMethod(root, "getNodeForAccessibilityId", this.id);
        } catch (Throwable t) {
            nodeInfo = null;
        }
        if (nodeInfo != null) {
            if (!nodeInfo.refresh()) {
                try {
                    nodeInfo.recycle();
                } catch (Throwable t) {}
                callback.call(null);
                return;
            }
        }
        callback.call(nodeInfo);
        if (nodeInfo != null) {
            try {
                nodeInfo.recycle();
            } catch (Throwable t) {}
        }
    }

    public interface GetAccessibilityNodeInfoCallbackRet<T> {
        T call(AccessibilityNodeInfo nodeInfo);
    }
    public<T> T getAccessibilityNodeInfoRet(GetAccessibilityNodeInfoCallbackRet<T> callback) {
        if (this.rootNodeGetter == null) {
            return callback.call(null);
        }
        if (this.aid == UNDEFINED_AID) {
            return callback.call(null);
        }
        AccessibilityNodeInfo nodeInfo;
        try {
            AccessibilityNodeInfo root = rootNodeGetter.getRootAccessibilityNodeInfo();
            if (root == null) {
                return callback.call(null);
            }
            nodeInfo = (AccessibilityNodeInfo) ReflectUtils.callMethod(root, "getNodeForAccessibilityId", this.id);
        } catch (Throwable t) {
            nodeInfo = null;
        }
        if (nodeInfo != null) {
            if (!nodeInfo.refresh()) {
                try {
                    nodeInfo.recycle();
                } catch (Throwable t) {}
                return callback.call(null);
            }
        }
        T ret = callback.call(nodeInfo);
        if (nodeInfo != null) {
            try {
                nodeInfo.recycle();
            } catch (Throwable t) {}
        }
        return ret;
    }

    public ViewInfo newCopy() {
        ViewInfo newOne = new ViewInfo();

        //long
        newOne.aid = this.aid;
        //long
        newOne.id = this.id;
        //String
        newOne.vid = this.vid;
        //int
        newOne.index = this.index;
        //String
        newOne.tag = this.tag;
        //String
        newOne.text = this.text;
        //String
        newOne.hint = this.hint;
        //String
        newOne.desc = this.desc;
        //String
        newOne.res = this.res;
        //String
        newOne.cls = this.cls;
        //String
        newOne.pkg = this.pkg;
        //Rect
        newOne.drawing_rect = new Rect(this.drawing_rect);
        //Rect
        newOne.bounds_on_screen = new Rect(this.bounds_on_screen);
        //String
        newOne.background = this.background;
        //String
        newOne.foreground = this.foreground;
        //int
        newOne.pos_in_adapter = this.pos_in_adapter;
        //String
        newOne.item_data_in_adapter = this.item_data_in_adapter;
        //boolean
        newOne.is_visible = this.is_visible;
        //boolean
        newOne.is_enable = this.is_enable;
        //boolean
        newOne.is_focusable = this.is_focusable;
        //boolean
        newOne.is_focused = this.is_focused;
        //boolean
        newOne.is_selected = this.is_selected;
        //boolean
        newOne.is_clickable = this.is_clickable;
        //boolean
        newOne.is_long_clickable = this.is_long_clickable;
        //boolean
        newOne.is_context_clickable = this.is_context_clickable;
        //boolean
        newOne.is_scrollable = this.is_scrollable;
        //boolean
        newOne.is_checked = this.is_checked;
        //boolean
        newOne.is_dismissable = this.is_dismissable;
        //boolean
        newOne.is_editable = this.is_editable;
        //boolean
        newOne.is_multiLine = this.is_multiLine;
        //boolean
        newOne.is_password = this.is_password;
        //Collection
        if (this.collection != null)
            newOne.collection = new Collection(this.collection);
        //CollectionItem
        if (this.collection_item != null)
            newOne.collection_item = new CollectionItem(this.collection_item);

        return newOne;
    }

    public static ViewInfo create(AccessibilityNodeInfo nodeInfo, RootAccessibilityNodeInfoGetter rootNodeGetter) {
        if (nodeInfo == null) return null;
        ViewInfo viewInfo = new ViewInfo();
        viewInfo.rootNodeGetter = rootNodeGetter;

        AccessibilityNodeInfo parent = nodeInfo.getParent();
        if (parent != null) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AccessibilityNodeInfo it = parent.getChild(i);
                if (nodeInfo.equals(it)) {
                    viewInfo.index = i;
                    break;
                }
                try {
                    it.recycle();
                } catch (Throwable t) {}
            }
            try {
                parent.recycle();
            } catch (Throwable t) {}
        }

        try {
            viewInfo.id = (long) ReflectUtils.callMethod(nodeInfo, "getSourceNodeId");
            viewInfo.aid = viewInfo.id;
            viewInfo.vid = String.valueOf(viewInfo.id);
        } catch (Throwable t) {}

        CharSequence text = nodeInfo.getText();
        viewInfo.text = text == null ? null : text.toString();
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            CharSequence hint = null;
            try {
                hint = (CharSequence) ReflectUtils.callMethod(nodeInfo, "getHintText");
            } catch (Throwable ignore) {}
            viewInfo.hint = hint == null ? null : hint.toString();
        }
        CharSequence desc = nodeInfo.getContentDescription();
        viewInfo.desc = desc == null ? null : desc.toString();
        viewInfo.res = nodeInfo.getViewIdResourceName();
        CharSequence clsName = nodeInfo.getClassName();
        viewInfo.cls = clsName == null ? null : clsName.toString();
        CharSequence pkgName = nodeInfo.getPackageName();
        viewInfo.pkg = pkgName == null ? null : pkgName.toString();

        viewInfo.drawing_rect = new Rect();
        nodeInfo.getBoundsInParent(viewInfo.drawing_rect);

        viewInfo.bounds_on_screen = new Rect();
        nodeInfo.getBoundsInScreen(viewInfo.bounds_on_screen);

        viewInfo.is_enable = nodeInfo.isEnabled();
        viewInfo.is_clickable = nodeInfo.isClickable();
        viewInfo.is_context_clickable = nodeInfo.isContextClickable();
        viewInfo.is_focusable = nodeInfo.isFocusable();
        viewInfo.is_long_clickable = nodeInfo.isLongClickable();
        viewInfo.is_selected = nodeInfo.isSelected();
        viewInfo.is_visible = nodeInfo.isVisibleToUser();
        viewInfo.is_scrollable = nodeInfo.isScrollable();
        viewInfo.is_checked = nodeInfo.isChecked();
        viewInfo.is_dismissable = nodeInfo.isScrollable();
        viewInfo.is_editable = nodeInfo.isScrollable();
        viewInfo.is_multiLine = nodeInfo.isScrollable();
        viewInfo.is_password = nodeInfo.isScrollable();

        AccessibilityNodeInfo.CollectionInfo collectionInfo = nodeInfo.getCollectionInfo();
        if (collectionInfo != null)
            viewInfo.collection = new Collection(collectionInfo);

        AccessibilityNodeInfo.CollectionItemInfo collectionItemInfo = nodeInfo.getCollectionItemInfo();
        if (collectionItemInfo != null)
            viewInfo.collection_item = new CollectionItem(collectionItemInfo);

        return viewInfo;
    }

    @Override
    public String toString() {
        return "ViewInfo{" +
                "id=" + id +
                ", aid=" + aid +
                ", vid='" + vid + '\'' +
                ", index=" + index +
                ", tag='" + tag + '\'' +
                ", text='" + text + '\'' +
                ", hint='" + hint + '\'' +
                ", desc='" + desc + '\'' +
                ", res='" + res + '\'' +
                ", cls='" + cls + '\'' +
                ", pkg='" + pkg + '\'' +
                ", drawing_rect=" + drawing_rect +
                ", bounds_on_screen=" + bounds_on_screen +
                ", is_visible=" + is_visible +
                ", is_enable=" + is_enable +
                ", is_focusable=" + is_focusable +
                ", is_focused=" + is_focused +
                ", is_selected=" + is_selected +
                ", is_clickable=" + is_clickable +
                ", is_long_clickable=" + is_long_clickable +
                ", is_context_clickable=" + is_context_clickable +
                ", is_scrollable=" + is_scrollable +
                ", is_checked=" + is_checked +
                ", is_dismissable=" + is_dismissable +
                ", is_editable=" + is_editable +
                ", is_multiLine=" + is_multiLine +
                ", is_password=" + is_password +
                ", collection=" + collection +
                ", collection_item=" + collection_item +
                '}';
    }
}
