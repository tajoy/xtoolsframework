package x.tools.framework.error;

public class ScriptRuntimeError extends XError {
    public ScriptRuntimeError() {
    }

    public ScriptRuntimeError(String message) {
        super(message);
    }

    public ScriptRuntimeError(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptRuntimeError(Throwable cause) {
        super(cause);
    }
}
