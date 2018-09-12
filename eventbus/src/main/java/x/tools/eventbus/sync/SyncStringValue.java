package x.tools.eventbus.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.eventbus.IEventBus;

public class SyncStringValue extends AbstractSyncValue<String> {
    public SyncStringValue(IEventBus eventBus, String id, String value) {
        super(eventBus, id, value);
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, String value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, value);
        }
    }

    @Override
    protected String getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return jsonObject.getString(key);
    }
}