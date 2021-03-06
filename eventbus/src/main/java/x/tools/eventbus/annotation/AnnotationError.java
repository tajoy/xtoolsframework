package x.tools.eventbus.annotation;

public class AnnotationError extends Exception {
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
