package x.tools.eventbus.json;

import java.lang.reflect.Method;

import x.tools.eventbus.EventBus;

public class GsonSerializer implements IJsonSerializer {
    private final Class class_Gson;
    private final Method method_toJson;
    private final Method method_fromJson;
    private final Object instance_Gson;

    public GsonSerializer() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        class_Gson = EventBus.getClassLoader().loadClass("com.google.gson.Gson");
        method_toJson = class_Gson.getDeclaredMethod("toJson", Object.class);
        method_fromJson = class_Gson.getDeclaredMethod("fromJson", String.class, Class.class);
        instance_Gson = class_Gson.newInstance();
    }

    @Override
    public <T> T fromJson(String json, Class<T> cls) {
        try {
            return (T) method_fromJson.invoke(instance_Gson, json, cls);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toJson(Object object) {
        try {
            return (String) method_toJson.invoke(instance_Gson, object);
        } catch (Exception e) {
            return null;
        }
    }
}
