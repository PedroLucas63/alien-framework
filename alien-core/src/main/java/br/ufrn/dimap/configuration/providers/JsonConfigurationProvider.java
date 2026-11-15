package br.ufrn.dimap.configuration.providers;

import br.ufrn.dimap.configuration.IConfigurationProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class JsonConfigurationProvider implements IConfigurationProvider {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final File file;
    private final Map<String, String> configuration = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public JsonConfigurationProvider(File file) {
        this.file = file;
    }

    @Override
    public void load() {
        configuration.clear();
        try {
            JsonNode root = MAPPER.readTree(file);
            if (root != null && root.isObject()) {
                for (var entry : root.properties()) {
                    populateConfiguration(entry.getKey(), entry.getValue(), null);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load JSON configuration from " + file.getPath(), e);
        }
    }

    private void populateConfiguration(String key, JsonNode value, String prefix) {
        String fullKey = (prefix == null || prefix.isBlank()) ? key : prefix + ":" + key;

        if (value.isObject()) {
            for (var child : value.properties()) {
                populateConfiguration(child.getKey(), child.getValue(), fullKey);
            }
        } else if (value.isArray()) {
            configuration.put(fullKey, parseJsonArray(value));
        } else if (value.isNull()) {
            configuration.put(fullKey, null);
        } else if (value.isValueNode()) {
            configuration.put(fullKey, value.asText());
        } else {
            throw new IllegalArgumentException("Unsupported JSON node type for key: " + fullKey);
        }
    }

    private static String parseJsonArray(JsonNode arrayNode) {
        List<String> elements = new ArrayList<>();
        for (JsonNode current : arrayNode) {
            if (current.isNull()) {
                elements.add("null");
            } else if (current.isValueNode()) {
                elements.add(current.asText());
            } else {
                throw new IllegalArgumentException("Unsupported nested object or array inside JSON array.");
            }
        }
        return String.join(",", elements);
    }

    @Override
    public Optional<String> tryGet(String key) {
        return Optional.ofNullable(configuration.get(key));
    }
}
