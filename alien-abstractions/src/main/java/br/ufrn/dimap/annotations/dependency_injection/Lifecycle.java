package br.ufrn.dimap.annotations.dependency_injection;

import br.ufrn.dimap.dependency_injection.types.LifecycleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Lifecycle {
    LifecycleType value();
}
