package br.ufrn.dimap.routing.types;

import java.util.HashMap;
import java.util.Map;

public record RouteMatchResult(
        boolean success,
        EndpointDescriptor endpoint,
        Map<String, String> routeValues
) {
   public static RouteMatchResult invalidMatch() {
       return new RouteMatchResult(false, null, new HashMap<>());
   }
}
