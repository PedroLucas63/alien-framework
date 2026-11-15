package br.ufrn.dimap.routing;

import br.ufrn.dimap.routing.types.RouteMatchResult;
import br.ufrn.dimap.types.HttpMethod;

public interface IRouteMatcher {
    RouteMatchResult match(HttpMethod method, String path);
}
