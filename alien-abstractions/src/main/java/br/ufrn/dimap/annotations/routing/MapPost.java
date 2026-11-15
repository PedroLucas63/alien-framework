package br.ufrn.dimap.annotations.routing;

import br.ufrn.dimap.types.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@MapRoute(methods = HttpMethod.Post)
public @interface MapPost {
    String value() default "";
}
