package x.tools.app;

import x.tools.framework.event.annotation.OnSyncValueUpdate;
import x.tools.framework.event.annotation.SyncValue;
import x.tools.framework.event.sync.AbstractSyncValue;
import x.tools.framework.event.sync.SyncStringValue;
import x.tools.framework.log.Loggable;

public class Status implements Loggable {
    private static Status INSTANCE = new Status();
    private Status() {
    }

    public static Status getInst() {
        return INSTANCE;
    }


    @SyncValue(value = "ok")
    private SyncStringValue status;

    @OnSyncValueUpdate(field = "status")
    public void onStatusUpdate(AbstractSyncValue syncValue, String oldValue, String newValue) {
        debug("onStatusUpdate %s -> %s", oldValue, newValue);
    }

    public String getStatus() {
        if (status == null) return null;
        return status.getValue();
    }

    public void setStatus(String status) {
        if (status == null) return;
        this.status.setValue(status);
    }
}
