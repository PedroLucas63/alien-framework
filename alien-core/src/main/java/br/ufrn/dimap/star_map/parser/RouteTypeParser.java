package br.ufrn.dimap.star_map.parser;

import java.util.Locale;
import java.util.UUID;

public final class RouteTypeParser {
    private RouteTypeParser() {}

    public static String getToken(Class<?> type) {
        if (type == int.class || type == Integer.class) return "int";
        if (type == String.class) return "str";
        if (type == UUID.class) return "guid";
        throw new IllegalStateException("Unsupported parameter type: " + type.getSimpleName());
    }

    public static String getRegexPattern(String typeToken) {
        return switch (typeToken.toLowerCase(Locale.ROOT)) {
            case "int" -> "^\\d+$";
            case "guid" -> "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
            case "str" -> "^[^/]+$";
            default -> throw new IllegalStateException("Invalid route parameter type: " + typeToken);
        };
    }

    public static Object parseValue(String typeToken, String value) {
        return switch (typeToken.toLowerCase(Locale.ROOT)) {
            case "str" -> value;
            case "int" -> {
                try {
                    yield Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Cannot parse '" + value + "' as int", e);
                }
            }
            case "guid" -> {
                try {
                    yield UUID.fromString(value);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Cannot parse '" + value + "' as guid", e);
                }
            }
            default -> throw new IllegalStateException("Invalid parameter type for parsing: " + typeToken);
        };
    }
}
