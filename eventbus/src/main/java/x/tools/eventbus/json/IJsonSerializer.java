package x.tools.eventbus.json;

public interface IJsonSerializer {
    <T> T fromJson(String json, Class<T> cls);
    String toJson(Object object);
}
