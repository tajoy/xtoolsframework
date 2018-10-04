package x.tools.eventbus.sync;


import org.json.JSONException;
import org.json.JSONObject;

import x.tools.eventbus.EventBus;
import x.tools.eventbus.IEventBus;

public class SyncObjectValue<T>  extends AbstractSyncValue<T> {
    public SyncObjectValue(IEventBus eventBus, String id, T value) {
        super(eventBus, id, value);
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, T value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, new JSONObject(EventBus.toJson(value)));
        }
    }

    @Override
    protected T getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return EventBus.fromJson(jsonObject.getJSONObject(key).toString(), getValueClass());
    }

}
