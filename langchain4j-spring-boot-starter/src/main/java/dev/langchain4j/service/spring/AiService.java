package dev.langchain4j.service.spring;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * TODO
 * TODO copy from AiServices
 * TODO Flux
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface AiService {

    /**
     * TODO
     */
    String chatModel() default "";

    // TODO make the rest of components configurable

    Class<?>[] tools() default {}; // TODO use all available tools by default?
}