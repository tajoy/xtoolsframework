package x.tools.framework.api;

import android.util.Range;

import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;

import x.tools.framework.annotation.PEnumInt;
import x.tools.framework.annotation.PFloatRange;
import x.tools.framework.annotation.PIntRange;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PNonNull;
import x.tools.framework.annotation.PNullable;

public class ParameterMetaInfo {
    private final String name;
    private final Class<?> type;
    private final Class<?> enumTarget;
    private final Range<Integer> intRange;
    private final Range<Float> floatRange;
    private final Boolean isNullable;

    public ParameterMetaInfo(Class<?> type, Annotation[] annotations) throws AnnotationError {
        String name = null;
        Class<?> enumTarget = null;
        Range<Integer> intRange = null;
        Range<Float> floatRange = null;
        Boolean isNullable = null;
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof PName) {
                    name = ((PName) annotation).name();
                    continue;
                }
                if (annotation instanceof PNonNull) {
                    if (isNullable != null) {
                        throw new AnnotationError("CANNOT use PNonNull or PNullable more then ONE");
                    }
                    if (type.isPrimitive()) {
                        throw new AnnotationError("CANNOT use PNonNull on primitive type");
                    }
                    isNullable = false;
                    continue;
                }
                if (annotation instanceof PNullable) {
                    if (isNullable != null) {
                        throw new AnnotationError("CANNOT use PNonNull or PNullable more then ONE");
                    }
                    if (type.isPrimitive()) {
                        throw new AnnotationError("CANNOT use PNullable on primitive type");
                    }
                    isNullable = true;
                    continue;
                }
                if (annotation instanceof PEnumInt) {
                    enumTarget = ((PEnumInt) annotation).target();
                    if (ClassUtils.isAssignable(type, Integer.class)) {
                        throw new AnnotationError("CANNOT use PEnumInt on " + type.getName() + " type");
                    }
                    continue;
                }
                if (annotation instanceof PFloatRange) {
                    floatRange = new Range<>(((PFloatRange) annotation).from(), ((PFloatRange) annotation).to());
                    if (ClassUtils.isAssignable(type, Float.class)) {
                        throw new AnnotationError("CANNOT use PFloatRange on " + type.getName() + " type");
                    }
                    continue;
                }
                if (annotation instanceof PIntRange) {
                    intRange = new Range<>(((PIntRange) annotation).from(), ((PIntRange) annotation).to());
                    if (ClassUtils.isAssignable(type, Integer.class)) {
                        throw new AnnotationError("CANNOT use PIntRange on " + type.getName() + " type");
                    }
                    continue;
                }
            }
        }

        if (name == null) {
            throw new AnnotationError("Api method MUST annotated with PName");
        }

        this.name = name;
        this.type = type;
        this.enumTarget = enumTarget;
        this.intRange = intRange;
        this.floatRange = floatRange;
        this.isNullable = isNullable;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Class<?> getEnumTarget() {
        return enumTarget;
    }

    public Range<Integer> getIntRange() {
        return intRange;
    }

    public Range<Float> getFloatRange() {
        return floatRange;
    }

    public Boolean getNullable() {
        return isNullable;
    }

    public void checkAnnotation(Object object) throws AnnotationError {
//        private final Boolean isNullable;
        try {
            if (enumTarget != null) {
                Object[] enumValues = enumTarget.getEnumConstants();
                int value = (Integer) object;
                boolean isIn = false;
                for (Object enumValue : enumValues) {
                    if (value == ((Enum) enumValue).ordinal()) {
                        isIn = true;
                        break;
                    }
                }
                if (!isIn)
                    throw new AnnotationError(
                            "enum int "
                                    + value
                                    + " doesn't contains in enum class "
                                    + enumTarget.getName()
                    );
            }
            if (intRange != null) {
                int value = (Integer) object;
                if (!intRange.contains(value)) {
                    throw new AnnotationError(
                            "int "
                                    + value
                                    + " doesn't between in range "
                                    + intRange.toString()
                    );
                }
            }
            if (floatRange != null) {
                float value = (Float) object;
                if (!floatRange.contains(value)) {
                    throw new AnnotationError(
                            "float "
                                    + value
                                    + " doesn't between in range "
                                    + floatRange.toString()
                    );
                }
            }
            if (isNullable != null) {
                if (!isNullable && object == null) {
                    throw new AnnotationError(
                            "object is null, but required non-null "
                    );
                }
            }
        } catch (AnnotationError e) {
            throw e;
        } catch (Throwable t) {
            throw new AnnotationError(t);
        }
    }
}
