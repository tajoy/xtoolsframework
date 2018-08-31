package x.tools.framework.error;

public class ParameterError extends XError {
    public ParameterError() {
    }

    public ParameterError(String message) {
        super(message);
    }

    public ParameterError(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterError(Throwable cause) {
        super(cause);
    }
}
