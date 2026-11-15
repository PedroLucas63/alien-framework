package br.ufrn.dimap.configuration;

import java.util.Optional;

public interface IConfiguration {
    void addFileSource(IFileConfigurationSource source);
    void addSource(IConfigurationSource source);
    void reload();
    void autoRegister();
    Optional<String> tryGet(String key);
    default <T> T bind(Class<T> type) {
        return bind(type, null);
    }
    <T> T bind(Class<T> type, String prefix);
}
