package br.ufrn.dimap.configuration;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.discovery.AssemblyDiscovery;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ConfigurationManager implements IConfiguration {
    private final IInversionOfControl services;
    private final List<IConfigurationProvider> providers;
    private final Map<String, Function<String, IConfigurationSource>> fileSourceFactories;
    private final Set<String> loadedFiles;
    private final Lock lock;

    public ConfigurationManager(IInversionOfControl services) {
        this.services = services;
        this.providers = new ArrayList<>();
        this.fileSourceFactories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.loadedFiles = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.lock = new ReentrantLock();
    }

    @Override
    public void addFileSource(IFileConfigurationSource source) {
        fileSourceFactories.put(source.extension(), source::create);
    }

    @Override
    public void addSource(IConfigurationSource source) {
        lock.lock();
        try {
            var provider = source.build();
            provider.load();
            providers.add(provider);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reload() {
        lock.lock();
        try {
            for (var provider : providers) {
                provider.load();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void autoRegister() {
        if (services == null)
            throw new IllegalStateException("Services cannot be null");
        autoRegister(services);
    }

    public void autoRegister(IInversionOfControl services) {
        for (var type : AssemblyDiscovery.getClasses())  {
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }

            var annotation = type.getAnnotation(ConfigurationProperties.class);
            if (annotation == null) {
                continue;
            }

            if (annotation.filename() != null && !annotation.filename().isBlank()) {
                var fullPath = Path.of(annotation.filename()).toAbsolutePath().normalize();
                if (loadedFiles.add(fullPath.toString())) {
                    String fileName = fullPath.getFileName().toString();
                    int dotIndex = fileName.lastIndexOf('.');
                    String extension = dotIndex >= 0 ? fileName.substring(dotIndex) : "";

                    var factory = fileSourceFactories.get(extension);
                    if (factory == null) {
                        throw new UnsupportedOperationException(
                                "No configuration source registered for extension '" + extension + "'."
                        );
                    }

                    addSource(factory.apply(fullPath.toString()));
                }
            }

            String prefix = annotation.prefix().isBlank() ? null : annotation.prefix();
            services.registerConfig(type, this, prefix);
        }
    }

    @Override
    public Optional<String> tryGet(String key) {
        lock.lock();
        try {
            for (int i = providers.size() - 1; i >= 0; i--) {
                var provider = providers.get(i);

                var value = provider.tryGet(key);
                if (value.isPresent()) {
                    return value;
                }
            }
        } finally {
            lock.unlock();
        }

        return Optional.empty();
    }

    @Override
    public <T> T bind(Class<T> type, String prefix) {
        try {
            T object = type.getDeclaredConstructor().newInstance();

            for (var field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }

                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                String key = (prefix == null || prefix.isBlank()) ?
                        field.getName() :
                        prefix + ":" + field.getName();

                if (isSimpleType(fieldType)) {
                    var rawValue = tryGet(key);
                    if (rawValue.isEmpty()) {
                        continue;
                    }

                    field.set(object, convertValue(rawValue.get(), fieldType));
                } else if (fieldType.isArray()) {
                    var rawValue = tryGet(key);
                    if (rawValue.isEmpty()) {
                        continue;
                    }

                    var elementType = fieldType.getComponentType();
                    var items = Arrays.stream(rawValue.get().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toArray(String[]::new);
                    var array = Array.newInstance(elementType, items.length);

                    for (int i = 0; i < items.length; i++) {
                        String item = items[i].trim();
                        if (!item.isEmpty()) {
                            Array.set(array, i, convertValue(item, elementType));
                        }
                    }

                    field.set(object, array);
                } else if (!fieldType.isInterface() && !fieldType.isPrimitive()) {
                    Object nestedObj = bind(fieldType, key);
                    field.set(object, nestedObj);
                }
            }

            return object;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not bind configuration to type " + type.getName(), e);
        }
    }

    private static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type.isPrimitive()
                || type == Integer.class || type == Long.class
                || type == Double.class || type == Float.class
                || type == Boolean.class || type == Short.class
                || type == Byte.class || type == Character.class
                || type.isEnum();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertValue(String raw, Class<?> targetType) {
        if (targetType == String.class) return raw;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(raw);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(raw);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(raw);
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(raw);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(raw);
        if (targetType == short.class || targetType == Short.class) return Short.parseShort(raw);
        if (targetType == byte.class || targetType == Byte.class) return Byte.parseByte(raw);
        if (targetType.isEnum()) return Enum.valueOf((Class<Enum>) targetType, raw);
        throw new IllegalArgumentException("Unsupported config type: " + targetType.getName());
    }

}
