package br.ufrn.dimap.configuration;

import java.util.Optional;

public interface IConfigurationProvider {
    void load();
    Optional<String> tryGet(String key);
}
