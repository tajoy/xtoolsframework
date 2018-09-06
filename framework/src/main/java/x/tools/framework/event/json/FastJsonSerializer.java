package x.tools.framework.event.json;

import java.lang.reflect.Method;

import x.tools.framework.event.GlobalEventBus;

public class FastJsonSerializer implements IJsonSerializer {
    private final Class class_JSON;
    private final Method method_toJSONString;
    private final Method method_parseObject;
    public FastJsonSerializer() throws ClassNotFoundException, NoSuchMethodException {
        class_JSON = GlobalEventBus.getClassLoader().loadClass("com.alibaba.fastjson.JSON");
        method_toJSONString = class_JSON.getDeclaredMethod("toJSONString", Object.class);
        method_parseObject = class_JSON.getDeclaredMethod("parseObject", String.class, Class.class);
    }

    @Override
    public <T> T fromJson(String json, Class<T> cls) {
        try {
            return (T) method_parseObject.invoke(class_JSON, json, cls);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toJson(Object object) {
        try {
            return (String) method_toJSONString.invoke(class_JSON, object);
        } catch (Exception e) {
            return null;
        }
    }
}
