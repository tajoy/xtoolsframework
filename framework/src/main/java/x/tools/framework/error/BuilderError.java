package x.tools.framework.error;

public class BuilderError extends XError {
    public BuilderError() {
    }

    public BuilderError(String message) {
        super(message);
    }

    public BuilderError(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderError(Throwable cause) {
        super(cause);
    }
}
