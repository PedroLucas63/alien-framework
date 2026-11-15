package br.ufrn.dimap.annotations.dependency_injection;

import br.ufrn.dimap.dependency_injection.types.LifecycleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Lifecycle(LifecycleType.PerUser)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PerUser {
    String name() default "";
    long leaseDurationMicroseconds() default 0;
    boolean passivateOnIdle() default true;
}
