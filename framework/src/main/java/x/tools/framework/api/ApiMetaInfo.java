package x.tools.framework.api;

import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ApiMetaInfo {
    private final String name;
    private final Method method;
    private final Field field;
    private final Class<?> type;
    private final ParameterMetaInfo[] parameterMetaInfo;

    public ApiMetaInfo(String name, Field field) throws AnnotationError {
        this.name = TextUtils.isEmpty(name) ? field.getName() : name;
        this.method = null;
        this.field = field;
        this.type = field.getType();
        this.parameterMetaInfo = null;
    }

    public ApiMetaInfo(String name,Method method) throws AnnotationError {
        this.name = TextUtils.isEmpty(name) ? method.getName() : name;
        this.method = method;
        this.field = null;
        this.type = method.getReturnType();

        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterTypes == null || parameterTypes.length <= 0
                || parameterAnnotations == null || parameterAnnotations.length <= 0) {
            this.parameterMetaInfo = new ParameterMetaInfo[0];
            return;
        }
        int parameterCount = parameterTypes.length;
        int annotationsCount = parameterAnnotations.length;
        int count = Math.min(parameterCount, annotationsCount);
        ParameterMetaInfo[] paramMetaInfo = new ParameterMetaInfo[count];
        for (int i = 0; i < count; i++) {
            paramMetaInfo[i] = new ParameterMetaInfo(parameterTypes[i], parameterAnnotations[i]);
        }
        this.parameterMetaInfo = paramMetaInfo;
    }


    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getType() {
        return type;
    }

    public ParameterMetaInfo[] getParameterMetaInfo() {
        return parameterMetaInfo;
    }
}
