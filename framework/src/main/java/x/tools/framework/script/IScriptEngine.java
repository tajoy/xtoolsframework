package x.tools.framework.script;

import org.json.JSONObject;

import x.tools.framework.XContext;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.error.XError;

public interface IScriptEngine {
    void init(XContext xContext) throws XError;
    void registerApi(AbstractApi abstractApi) throws XError;

    void runScript(String name, String script, IScriptValue... args) throws XError;
    void runScriptFile(String filename, IScriptValue... args) throws XError;

    void dispatchEvent(String name, JSONObject data) throws XError;

    <T> IScriptValue createValue(T value) throws XError;
}
