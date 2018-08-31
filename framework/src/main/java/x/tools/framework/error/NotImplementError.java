package x.tools.framework.error;

import java.util.Locale;

import x.tools.framework.XContext;

public class NotImplementError extends XError {
    public NotImplementError(Class cls) {
        super(String.format(
                Locale.getDefault(),
                "Not Implement %s::%s",
                cls,
                Thread.currentThread().getStackTrace()[1].getMethodName()
                )
        );
    }
}
