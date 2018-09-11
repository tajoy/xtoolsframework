package x.tools.framework.event.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.framework.event.IEventBus;

public class SyncLongValue extends AbstractSyncValue<Long> {
    public SyncLongValue(IEventBus eventBus, String id, Long value) {
        super(eventBus, id, value);
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, Long value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, (long) value);
        }
    }

    @Override
    protected Long getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return jsonObject.getLong(key);
    }
}