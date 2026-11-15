package br.ufrn.dimap.dependency_injection.types;

public record ServiceKey(Class<?> type, String name) {
    public ServiceKey(Class<?> type) {
        this(type, null);
    }
}
