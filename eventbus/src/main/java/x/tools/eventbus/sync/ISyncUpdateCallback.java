package x.tools.eventbus.sync;

import x.tools.eventbus.annotation.ThreadMode;

public interface ISyncUpdateCallback {
    ThreadMode threadMode();
    <T> void onUpdate(AbstractSyncValue<T> syncValue, T oldValue, T newValue) throws Throwable;
}
