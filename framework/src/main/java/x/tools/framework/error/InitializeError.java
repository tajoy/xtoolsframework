package x.tools.framework.error;

import x.tools.framework.error.XError;

public class InitializeError extends XError {
    public InitializeError() {
    }

    public InitializeError(String message) {
        super(message);
    }

    public InitializeError(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeError(Throwable cause) {
        super(cause);
    }
}
