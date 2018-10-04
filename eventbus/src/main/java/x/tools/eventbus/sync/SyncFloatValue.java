package x.tools.eventbus.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.eventbus.IEventBus;

public class SyncFloatValue extends AbstractSyncValue<Float> {
    public SyncFloatValue(IEventBus eventBus, String id, Float value) {
        super(eventBus, id, value);
    }

    @Override
    public Class<Float> getValueClass() {
        return Float.class;
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, Float value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, (float) value);
        }
    }

    @Override
    protected Float getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return (float) jsonObject.getDouble(key);
    }
}