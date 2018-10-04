package x.tools.eventbus.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.eventbus.IEventBus;

public class SyncDoubleValue extends AbstractSyncValue<Double> {
    public SyncDoubleValue(IEventBus eventBus, String id, Double value) {
        super(eventBus, id, value);
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, Double value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, (double) value);
        }
    }

    @Override
    protected Double getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return jsonObject.getDouble(key);
    }
}