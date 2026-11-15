package br.ufrn.dimap.star_map.types;

public record RouteSegment(
        String name,
        SegmentType type,
        String parameterType
) {
    public RouteSegment(String name, SegmentType type) {
        this(name, type, null);
    }

    public static RouteSegment parse(String rawSegment) {
        if ("*".equals(rawSegment)) {
            return new RouteSegment(rawSegment, SegmentType.Wildcard);
        }

        boolean isParam = rawSegment.startsWith("{") && rawSegment.endsWith("}");
        if (!isParam) {
            return new RouteSegment(rawSegment, SegmentType.Comum);
        }

        String inner = rawSegment.substring(1, rawSegment.length() - 1);
        String[] parts = inner.split(":", 2);

        String name = parts[0];
        String parameterType = parts.length == 2 ? parts[1] : "str";

        return new RouteSegment(name, SegmentType.Parameter, parameterType);
    }
}
