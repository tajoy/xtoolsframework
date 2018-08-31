package x.tools.framework.error;

public abstract class XError extends Exception {
    public XError() {
        super();
    }

    public XError(String message) {
        super(message);
    }

    public XError(String message, Throwable cause) {
        super(message, cause);
    }

    public XError(Throwable cause) {
        super(cause);
    }
}
