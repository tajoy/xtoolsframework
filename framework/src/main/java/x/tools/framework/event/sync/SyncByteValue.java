package x.tools.framework.event.sync;

import org.json.JSONException;
import org.json.JSONObject;

import x.tools.framework.event.IEventBus;

public class SyncByteValue extends AbstractSyncValue<Byte> {
    public SyncByteValue(IEventBus eventBus, String id, Byte value) {
        super(eventBus, id, value);
    }

    @Override
    protected void putValue(JSONObject jsonObject, String key, Byte value) throws JSONException {
        if (value == null) {
            jsonObject.put(key, JSONObject.NULL);
        } else {
            jsonObject.put(key, (int)value);
        }
    }

    @Override
    protected Byte getValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.isNull(key)) {
            return null;
        }
        return (byte) jsonObject.getInt(key);
    }
}