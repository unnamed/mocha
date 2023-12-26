package team.unnamed.mocha.runtime.binding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(BindExternalFunction.Multiple.class)
public @interface BindExternalFunction {
    Class<?> at();

    String name();

    String as() default "";

    Class<?>[] args();

    @Documented
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Multiple {
        BindExternalFunction[] value();
    }
}
