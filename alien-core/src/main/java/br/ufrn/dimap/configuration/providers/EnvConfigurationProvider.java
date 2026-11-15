package br.ufrn.dimap.configuration.providers;

import br.ufrn.dimap.configuration.IConfigurationProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class EnvConfigurationProvider implements IConfigurationProvider {
    private static final String SPECIAL_FILE_ENV = ".env";

    private final String prefix;
    private final Map<String, String> configuration = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public EnvConfigurationProvider() {
        this(null);
    }

    public EnvConfigurationProvider(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public void load() {
        configuration.clear();
        Path envPath = Path.of(SPECIAL_FILE_ENV);
        if (Files.exists(envPath)) {
            readEnvFile(envPath);
        }
        readSystemVariables();
    }

    private void readEnvFile(Path envPath) {
        try {
            for (String line : Files.readAllLines(envPath)) {
                String current = line.trim();
                if (current.isEmpty() || current.startsWith("#")) {
                    continue;
                }

                String[] parts = current.split("=", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid environment variable format.");
                }

                String normalizedKey = normalizeKey(parts[0].trim());
                if (normalizedKey == null) {
                    continue;
                }

                String rawValue = getValue(parts);
                configuration.put(normalizedKey, rawValue);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read .env file.", e);
        }
    }

    private static String getValue(String[] parts) {
        String rawValue = parts[1].trim();
        if (rawValue.startsWith("\"")) {
            int closingQuoteIndex = rawValue.indexOf('"', 1);
            if (closingQuoteIndex == -1) {
                throw new IllegalArgumentException("Invalid environment variable format.");
            }
            rawValue = rawValue.substring(1, closingQuoteIndex);
        } else {
            int commentIndex = rawValue.indexOf('#');
            if (commentIndex != -1) {
                rawValue = rawValue.substring(0, commentIndex).trim();
            }
        }
        return rawValue;
    }

    private void readSystemVariables() {
        for (var entry : System.getenv().entrySet()) {
            String normalizedKey = normalizeKey(entry.getKey());
            if (normalizedKey == null) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue();
            configuration.put(normalizedKey, value);
        }
    }

    private String normalizeKey(String key) {
        if (!prefix.isEmpty() && !key.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
            return null;
        }
        String withoutPrefix = key.substring(prefix.length());
        int start = 0;
        while (start < withoutPrefix.length() && withoutPrefix.charAt(start) == '_') {
            start++;
        }
        return withoutPrefix.substring(start).replace("__", ":");
    }

    @Override
    public Optional<String> tryGet(String key) {
        return Optional.ofNullable(configuration.get(key));
    }
}
