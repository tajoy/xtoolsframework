package x.tools.framework.script;

import x.tools.framework.XContext;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.error.XError;

public interface IScriptEngine {
    void init(XContext xContext) throws XError;
    void registerApi(AbstractApi abstractApi) throws XError;

    void runScript(String name, String script, IScriptValue... args) throws XError;
    void runScriptFile(String filename, IScriptValue... args) throws XError;

    void addEventListener(String name, IScriptCallback callback) throws XError;
    void removeEventListener(String name, IScriptCallback callback) throws XError;
    IScriptValue dispatchEvent(String name, IScriptValue... args) throws XError;

    <T> IScriptValue createValue(T value) throws XError;
}
