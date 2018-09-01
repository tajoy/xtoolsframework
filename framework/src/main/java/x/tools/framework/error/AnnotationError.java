package x.tools.framework.error;

import x.tools.framework.error.XError;

public final class AnnotationError extends XError {
    public AnnotationError() {
    }

    public AnnotationError(String message) {
        super(message);
    }

    public AnnotationError(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationError(Throwable cause) {
        super(cause);
    }
}
