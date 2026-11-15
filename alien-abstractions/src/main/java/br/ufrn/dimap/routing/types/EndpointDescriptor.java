package br.ufrn.dimap.routing.types;

import br.ufrn.dimap.types.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public record EndpointDescriptor(
        HttpMethod method,
        String pathPattern,
        Class<?> controllerType,
        Method methodFunction,
        List<Parameter> parameters,
        List<Annotation> metadata
) {

}