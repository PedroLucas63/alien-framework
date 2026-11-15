package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.Content;
import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Shared helper utilities for {@link br.ufrn.dimap.pipeline.IParameterBinder} implementations.
 */
final class BinderUtils {

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.ofEntries(
            Map.entry(String.class, s -> s),
            Map.entry(int.class, Integer::parseInt),
            Map.entry(Integer.class, Integer::valueOf),
            Map.entry(long.class, Long::parseLong),
            Map.entry(Long.class, Long::valueOf),
            Map.entry(double.class, Double::parseDouble),
            Map.entry(Double.class, Double::valueOf),
            Map.entry(float.class, Float::parseFloat),
            Map.entry(Float.class, Float::valueOf),
            Map.entry(boolean.class, Boolean::parseBoolean),
            Map.entry(Boolean.class, Boolean::valueOf),
            Map.entry(short.class, Short::parseShort),
            Map.entry(Short.class, Short::valueOf),
            Map.entry(byte.class, Byte::parseByte),
            Map.entry(Byte.class, Byte::valueOf),
            Map.entry(UUID.class, UUID::fromString)
    );

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            boolean.class, false,
            byte.class, (byte) 0,
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0d,
            char.class, '\0'
    );

    private BinderUtils() {
    }

    static boolean hasOtherContentAnnotation(Parameter parameter, Class<? extends Annotation> allowedAnnotation) {
        for (Annotation annotation : parameter.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.isAnnotationPresent(Content.class) && !type.equals(allowedAnnotation)) {
                return true;
            }
        }
        return false;
    }

    static String resolveParameterName(Parameter parameter, String explicitName) {
        if (explicitName != null && !explicitName.isBlank()) {
            return explicitName;
        }
        if (!parameter.isNamePresent()) {
            throw new IllegalStateException(
                    "Parameter name is not available for '" + parameter + "'. "
                            + "Compile the application with <parameters>true</parameters> (javac -parameters) "
                            + "or specify an explicit name in the binding annotation."
            );
        }
        return parameter.getName();
    }

    static boolean isSupportedType(Class<?> targetType) {
        return CONVERTERS.containsKey(targetType) || targetType.isEnum();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Optional<Object> tryConvert(String raw, Class<?> targetType) {
        if (raw == null) {
            return Optional.empty();
        }
        Function<String, Object> converter = CONVERTERS.get(targetType);
        if (converter != null) {
            try {
                return Optional.of(converter.apply(raw));
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }
        if (targetType.isEnum()) {
            try {
                return Optional.of(Enum.valueOf((Class<Enum>) targetType, raw));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    static Object defaultValue(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_DEFAULTS.get(type) : null;
    }

    static void chainOnFinish(ProcessContext context, Runnable action) {
        Runnable existing = context.onFinish;
        context.onFinish = existing == null
                ? action
                : () -> {
                    try {
                        existing.run();
                    } finally {
                        action.run();
                    }
                };
    }
}
