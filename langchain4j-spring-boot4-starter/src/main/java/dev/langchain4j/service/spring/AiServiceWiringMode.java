package dev.langchain4j.service.spring;

/**
 * Specifies how LangChain4j components are wired (injected) into a given AI Service.
 */
public enum AiServiceWiringMode {

    /**
     * All LangChain4j components available in the application context are wired automatically into a given AI Service.
     * If there are multiple components of the same type, an exception is thrown.
     */
    AUTOMATIC,

    /**
     * Only explicitly specified LangChain4j components are wired into a given AI Service.
     * Component (bean) names are specified using attributes of {@link AiService} annotation like this:
     * {@code AiService(wiringMode = EXPLICIT, chatMemory = "<name of a ChatMemory bean>")}
     */
    EXPLICIT
}