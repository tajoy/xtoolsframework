package x.tools.framework.event.json;

public interface IJsonSerializer {
    <T> T fromJson(String json, Class<T> cls);
    String toJson(Object object);
}
