package br.ufrn.dimap.star_map.types;

import br.ufrn.dimap.routing.types.EndpointDescriptor;
import br.ufrn.dimap.routing.types.RouteMatchResult;
import br.ufrn.dimap.star_map.parser.RouteTypeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RouteTree {
    private final RouteNode root = new RouteNode("", "/");
    private final List<EndpointDescriptor> endpointDescriptors = new ArrayList<>();

    public void addRoute(String path, EndpointDescriptor descriptor) {
        List<String> segments = splitSegments(path);
        RouteNode current = root;
        String currentPath = current.getPath();

        for (String segment : segments) {
            currentPath += segment + "/";
            RouteSegment routeSegment = RouteSegment.parse(segment);

            if (current.isWildcard()) {
                throw new IllegalArgumentException("Cannot add a route with wildcard segment '" + segment + "'");
            }

            switch (routeSegment.type()) {
                case Comum -> {
                    RouteNode nextNode = current.getChildren().get(segment);
                    if (nextNode == null) {
                        nextNode = new RouteNode(segment, currentPath);
                        current.addChild(nextNode);
                    }
                    current = nextNode;
                }
                case Parameter -> {
                    if (routeSegment.parameterType() == null) {
                        throw new IllegalArgumentException("Cannot add a parameter route without a parameter type");
                    }
                    String regexPattern = RouteTypeParser.getRegexPattern(routeSegment.parameterType());
                    RouteNode nextNode = current.getParameterChildren().get(regexPattern);
                    if (nextNode == null) {
                        nextNode = new RouteNode(routeSegment.name(), currentPath, Pattern.compile(regexPattern));
                        current.addParameterChild(nextNode);
                    }

                    if (!nextNode.getSegment().equals(routeSegment.name())) {
                        throw new IllegalArgumentException("Cannot add routes with different segments");
                    }
                    current = nextNode;
                }
                case Wildcard -> current.setWildcard(true);
            }
        }

        current.setDescriptor(descriptor);
        endpointDescriptors.add(descriptor);
    }

    public RouteMatchResult match(String path) {
        List<String> segments = splitSegments(path);
        RouteNode current = root;
        boolean success = true;
        Map<String, String> routeValues = new HashMap<>();

        if (root.isWildcard()) {
            return new RouteMatchResult(true, current.getDescriptor(), routeValues);
        }

        for (String segment : segments) {
            var nextOpt = current.tryGetNode(segment);
            if (nextOpt.isEmpty()) {
                success = false;
                break;
            }

            current = nextOpt.get();
            if (current.getSegmentRegex() != null) {
                routeValues.put(current.getSegment(), segment);
            }

            if (current.isWildcard()) {
                break;
            }
        }

        if (!success || current.getDescriptor() == null) {
            return RouteMatchResult.invalidMatch();
        }

        return new RouteMatchResult(true, current.getDescriptor(), routeValues);
    }

    public List<EndpointDescriptor> getEndpoints() {
        return List.copyOf(endpointDescriptors);
    }

    private static List<String> splitSegments(String path) {
        if (path == null || path.isBlank()) {
            return List.of();
        }
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
