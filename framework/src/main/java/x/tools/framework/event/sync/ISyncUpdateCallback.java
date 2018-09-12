package x.tools.framework.event.sync;

import x.tools.framework.event.annotation.ThreadMode;

public interface ISyncUpdateCallback {
    ThreadMode threadMode();
    <T> void onUpdate(AbstractSyncValue<T> syncValue, T oldValue, T newValue) throws Throwable;
}
