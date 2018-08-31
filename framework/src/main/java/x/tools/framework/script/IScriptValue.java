package x.tools.framework.script;

import x.tools.framework.error.ScriptValueConvertError;

public interface IScriptValue {
    String getTypeName();
    Object convertTo() throws ScriptValueConvertError;
    <T> T convertTo(Class<T> cls) throws ScriptValueConvertError;
    String toString();
}
