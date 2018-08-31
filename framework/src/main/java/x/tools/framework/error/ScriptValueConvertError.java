package x.tools.framework.error;

import x.tools.framework.XContext;

public final class ScriptValueConvertError extends XError {
    public ScriptValueConvertError() {
        super();
    }

    public ScriptValueConvertError(String message) {
        super(message);
    }

    public ScriptValueConvertError(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptValueConvertError(Throwable cause) {
        super(cause);
    }
}
