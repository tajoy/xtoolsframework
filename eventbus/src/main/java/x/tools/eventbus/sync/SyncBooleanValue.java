package x.tools.eventbus.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.eventbus.IEventBus;

public class SyncBooleanValue extends AbstractSyncValue<Boolean> {

    public SyncBooleanValue(IEventBus eventBus, String id, Boolean value) {
        super(eventBus, id, value);
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, Boolean value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, (boolean) value);
        }
    }

    @Override
    protected Boolean getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return jsonObject.getBoolean(key);
    }
}
