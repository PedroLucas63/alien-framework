package br.ufrn.dimap.star_map;

import br.ufrn.dimap.annotations.binding.Param;
import br.ufrn.dimap.annotations.routing.Controller;
import br.ufrn.dimap.annotations.routing.MapRoute;
import br.ufrn.dimap.discovery.AssemblyDiscovery;
import br.ufrn.dimap.routing.IRouteMatcher;
import br.ufrn.dimap.routing.IRouteRegistry;
import br.ufrn.dimap.routing.types.EndpointDescriptor;
import br.ufrn.dimap.routing.types.RouteMatchResult;
import br.ufrn.dimap.star_map.parser.RouteTypeParser;
import br.ufrn.dimap.star_map.types.RouteSegment;
import br.ufrn.dimap.star_map.types.RouteTree;
import br.ufrn.dimap.star_map.types.SegmentType;
import br.ufrn.dimap.types.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StarMapManager implements IRouteRegistry, IRouteMatcher {
    private final Map<HttpMethod, RouteTree> methods = new EnumMap<>(HttpMethod.class);
    private final List<EndpointDescriptor> endpoints = new ArrayList<>();

    private record RouteMapping(HttpMethod[] httpMethods, String path) {}

    @Override
    public void autoRegister() {
        for (Class<?> type : AssemblyDiscovery.getClasses()) {
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            if (!type.isAnnotationPresent(Controller.class)) {
                continue;
            }
            registerController(type);
        }
    }

    @Override
    public <TController> void registerController(Class<TController> controllerType) {
        Controller controllerAnnotation = controllerType.getAnnotation(Controller.class);
        if (controllerAnnotation == null) {
            throw new IllegalStateException("Controller annotation could not be found on " + controllerType.getName());
        }

        String cleanBase = trimSlashes(controllerAnnotation.value());
        String basePath = cleanBase.isEmpty() ? "" : "/" + cleanBase;

        for (Method method : controllerType.getMethods()) {
            List<RouteMapping> mappings = extractRouteMappings(method);
            if (mappings.isEmpty()) {
                continue;
            }

            for (RouteMapping mapping : mappings) {
                String routePath = stripLeadingSlashes(adjustPath(mapping.path(), method));
                String parsedPath = routePath.isEmpty()
                        ? (basePath.isEmpty() ? "/" : basePath)
                        : basePath + "/" + routePath;

                HttpMethod[] targetMethods = mapping.httpMethods().length > 0
                        ? mapping.httpMethods()
                        : new HttpMethod[]{null};

                for (HttpMethod httpMethod : targetMethods) {
                    var descriptor = new EndpointDescriptor(
                            httpMethod,
                            parsedPath,
                            controllerType,
                            method,
                            List.of(method.getParameters()),
                            List.of(method.getAnnotations())
                    );
                    addRouteToTrees(parsedPath, descriptor);
                }
            }
        }
    }

    @Override
    public void mapEndpoint(EndpointDescriptor descriptor) {
        String parsedPath = adjustPath(descriptor.pathPattern(), descriptor.methodFunction());
        var updated = new EndpointDescriptor(
                descriptor.method(),
                parsedPath,
                descriptor.controllerType(),
                descriptor.methodFunction(),
                descriptor.parameters(),
                descriptor.metadata()
        );
        addRouteToTrees(parsedPath, updated);
    }

    private void addRouteToTrees(String parsedPath, EndpointDescriptor descriptor) {
        HttpMethod[] targetMethods = descriptor.method() != null
                ? new HttpMethod[]{descriptor.method()}
                : HttpMethod.values();

        for (HttpMethod method : targetMethods) {
            RouteTree tree = methods.computeIfAbsent(method, k -> new RouteTree());
            tree.addRoute(parsedPath, descriptor);
        }

        endpoints.add(descriptor);
    }

    @Override
    public List<EndpointDescriptor> getEndpoints() {
        return List.copyOf(endpoints);
    }

    @Override
    public RouteMatchResult match(HttpMethod method, String path) {
        RouteTree tree = methods.get(method);
        return tree == null ? RouteMatchResult.invalidMatch() : tree.match(path);
    }

    private static List<RouteMapping> extractRouteMappings(Method method) {
        List<RouteMapping> mappings = new ArrayList<>();

        for (MapRoute direct : method.getAnnotationsByType(MapRoute.class)) {
            mappings.add(new RouteMapping(direct.methods(), direct.path()));
        }

        for (Annotation annotation : method.getAnnotations()) {
            Class<? extends Annotation> annType = annotation.annotationType();
            MapRoute metaRoute = annType.getAnnotation(MapRoute.class);
            if (metaRoute == null) {
                continue;
            }

            String path = readValueAttribute(annotation);
            if (path.isEmpty()) {
                path = metaRoute.path();
            }
            mappings.add(new RouteMapping(metaRoute.methods(), path));
        }

        return mappings;
    }

    private static String readValueAttribute(Annotation annotation) {
        try {
            Method valueMethod = annotation.annotationType().getMethod("value");
            Object result = valueMethod.invoke(annotation);
            return result instanceof String str ? str : "";
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private static String adjustPath(String path, Method method) {
        List<String> segments = Arrays.stream((path == null ? "" : path).trim().split("/"))
                .filter(s -> !s.isEmpty())
                .toList();

        Parameter[] parameters = method.getParameters();
        Set<String> pathParams = new HashSet<>();
        List<String> adjustedSegments = new ArrayList<>();

        for (String segment : segments) {
            RouteSegment parsed = RouteSegment.parse(segment);

            if (parsed.type() == SegmentType.Parameter) {
                String paramName = parsed.name();
                if (!pathParams.add(paramName)) {
                    throw new IllegalStateException("The parameter '" + paramName + "' has already been added");
                }

                Parameter paramInfo = findMatchingParameter(parameters, paramName);
                if (paramInfo == null) {
                    boolean missingNames = Arrays.stream(parameters).anyMatch(p -> !p.isNamePresent());
                    if (missingNames) {
                        throw new IllegalStateException(
                                "Parameter '" + paramName + "' not found on method '" + method.getName()
                                        + "'. Parameter names are not present in bytecode: compile with -parameters "
                                        + "(<parameters>true</parameters> in Maven) or annotate with @Param(\""
                                        + paramName + "\")."
                        );
                    }
                    throw new IllegalStateException("Parameter " + paramName + " not found.");
                }

                String typeToken = RouteTypeParser.getToken(paramInfo.getType());
                adjustedSegments.add("{" + paramName + ":" + typeToken + "}");
            } else {
                adjustedSegments.add(segment);
            }
        }

        return "/" + String.join("/", adjustedSegments);
    }

    private static Parameter findMatchingParameter(Parameter[] parameters, String paramName) {
        for (Parameter p : parameters) {
            if (p.isNamePresent() && p.getName().equals(paramName)) {
                return p;
            }
            Param paramAnnotation = p.getAnnotation(Param.class);
            if (paramAnnotation != null && paramAnnotation.value().equals(paramName)) {
                return p;
            }
        }
        return null;
    }

    private static String trimSlashes(String value) {
        if (value == null) return "";
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '/') start++;
        while (end > start && value.charAt(end - 1) == '/') end--;
        return value.substring(start, end);
    }

    private static String stripLeadingSlashes(String value) {
        if (value == null) return "";
        int start = 0;
        while (start < value.length() && value.charAt(start) == '/') start++;
        return value.substring(start);
    }
}
