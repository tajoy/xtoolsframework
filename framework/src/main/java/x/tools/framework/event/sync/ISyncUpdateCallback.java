package x.tools.framework.event.sync;

public interface ISyncUpdateCallback {
    <T> void onUpdate(AbstractSyncValue<T> syncValue, T oldValue, T newValue) throws Throwable;
}
