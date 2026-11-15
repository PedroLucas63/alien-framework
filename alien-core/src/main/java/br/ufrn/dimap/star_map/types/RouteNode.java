package br.ufrn.dimap.star_map.types;

import br.ufrn.dimap.routing.types.EndpointDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class RouteNode {
    private final String segment;
    private final String path;
    private final Pattern segmentRegex;
    private final Map<String, RouteNode> children = new HashMap<>();
    private final Map<String, RouteNode> parameterChildren = new HashMap<>();

    private EndpointDescriptor descriptor;
    private boolean isWildcard = false;

    public RouteNode(String segment, String path, Pattern segmentRegex) {
        this.segment = segment;
        this.path = path;
        this.segmentRegex = segmentRegex;
    }

    public RouteNode(String segment, String path) {
        this(segment, path, null);
    }

    public String getSegment() {
        return segment;
    }

    public String getPath() {
        return path;
    }

    public Pattern getSegmentRegex() {
        return segmentRegex;
    }

    public EndpointDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(EndpointDescriptor descriptor) {
        if (this.descriptor != null) {
            throw new IllegalArgumentException("Descriptor is already set");
        }
        this.descriptor = descriptor;
    }

    public Map<String, RouteNode> getChildren() {
        return children;
    }

    public Map<String, RouteNode> getParameterChildren() {
        return parameterChildren;
    }

    public boolean isWildcard() {
        return isWildcard;
    }

    public void setWildcard(boolean wildcard) {
        if (wildcard && (!children.isEmpty() || !parameterChildren.isEmpty() || descriptor != null)) {
            throw new IllegalArgumentException("Cannot set wildcard on a node that already has children");
        }
        this.isWildcard = wildcard;
    }

    public void addChild(RouteNode child) {
        children.put(child.getSegment(), child);
    }

    public void addParameterChild(RouteNode child) {
        if (child.getSegmentRegex() == null) {
            throw new IllegalArgumentException("Segment regex is null");
        }
        parameterChildren.put(child.getSegmentRegex().pattern(), child);
    }

    public Optional<RouteNode> tryGetNode(String segment) {
        RouteNode direct = children.get(segment);
        if (direct != null) {
            return Optional.of(direct);
        }

        for (RouteNode parameterChild : parameterChildren.values()) {
            if (parameterChild.getSegmentRegex().matcher(segment).matches()) {
                return Optional.of(parameterChild);
            }
        }

        return Optional.empty();
    }
}
