package x.tools.eventbus;

import org.json.JSONObject;

public interface IEventSender {

    /**
     * 发送事件填充 source 的 id
     * @return 本地 id
     */
    String getId();

    /**
     * 触发事件
     * @param event 事件
     */
    void trigger(Event event);

    /**
     * 触发事件, 无数据
     * @param name 事件名称
     */
    void trigger(String name);

    /**
     *  触发事件, 带有数据, 自动通过 json 序列化 data
     *  序列化参见: IJsonSerializer
     *
     * @param name 事件名称
     * @param data 事件数据
     */
    void trigger(String name, Object data);

    /**
     *  触发事件, 带有数据, 原始的 json 数据
     *  这个接口主要方便脚本触发的事件
     *
     * @param name 事件名称
     * @param data 事件数据(JSON)
     */
    void triggerRaw(String name, JSONObject data);
}
