package pixlze.guildapi.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SyncConfigurable {
    String syncUri();

    // should only be used when value of config is int, where int is the index of the cycle.
    int cycleLength() default 0;

    String i18nKey() default "";
}
