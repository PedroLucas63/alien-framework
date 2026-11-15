package br.ufrn.dimap.annotations.routing;

import br.ufrn.dimap.types.HttpMethod;

import java.lang.annotation.*;
import java.util.Optional;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Repeatable(MapRoutes.class)
public @interface MapRoute {
    HttpMethod[] methods() default {};
    String path() default "";
}
