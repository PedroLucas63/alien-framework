package br.ufrn.dimap.annotations.dependency_injection;

import br.ufrn.dimap.dependency_injection.types.LifecycleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Lifecycle(LifecycleType.PerRequest)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PerRequest {
    String name() default "";
    boolean withPool() default false;
    int poolSize() default 10;
}
