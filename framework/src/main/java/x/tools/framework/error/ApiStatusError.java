package x.tools.framework.error;

public class ApiStatusError extends XError {
    public ApiStatusError() {
    }

    public ApiStatusError(String message) {
        super(message);
    }

    public ApiStatusError(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiStatusError(Throwable cause) {
        super(cause);
    }
}
