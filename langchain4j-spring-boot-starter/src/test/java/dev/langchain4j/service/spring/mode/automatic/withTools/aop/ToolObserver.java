package dev.langchain4j.service.spring.mode.automatic.withTools.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolObserver {

    /**
     * key just for example
     *
     * @return the key
     */
    String key();
}
