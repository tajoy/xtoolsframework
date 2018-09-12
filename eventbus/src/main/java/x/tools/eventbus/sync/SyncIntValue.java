package x.tools.eventbus.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.eventbus.IEventBus;

public class SyncIntValue extends AbstractSyncValue<Integer> {
    public SyncIntValue(IEventBus eventBus, String id, Integer value) {
        super(eventBus, id, value);
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, Integer value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, (int) value);
        }
    }

    @Override
    protected Integer getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return jsonObject.getInt(key);
    }
}
