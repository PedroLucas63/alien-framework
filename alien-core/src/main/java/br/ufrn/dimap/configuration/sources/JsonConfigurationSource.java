package br.ufrn.dimap.configuration.sources;

import br.ufrn.dimap.configuration.IConfigurationProvider;
import br.ufrn.dimap.configuration.IConfigurationSource;
import br.ufrn.dimap.configuration.IFileConfigurationSource;
import br.ufrn.dimap.configuration.providers.JsonConfigurationProvider;

import java.io.File;
import java.util.Locale;

public class JsonConfigurationSource implements IFileConfigurationSource {
    private static final String EXTENSION = ".json";
    private final String path;

    public JsonConfigurationSource() {
        this.path = null;
    }

    public JsonConfigurationSource(String path) {
        this.path = path;
    }

    @Override
    public String extension() {
        return EXTENSION;
    }

    @Override
    public IConfigurationSource create(String path) {
        return new JsonConfigurationSource(path);
    }

    @Override
    public IConfigurationProvider build() {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Configuration file path cannot be null or empty.");
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("The path " + path + " does not exist.");
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(EXTENSION)) {
            throw new IllegalArgumentException("The path " + path + " does not end with the extension " + EXTENSION + ".");
        }

        return new JsonConfigurationProvider(file);
    }
}
