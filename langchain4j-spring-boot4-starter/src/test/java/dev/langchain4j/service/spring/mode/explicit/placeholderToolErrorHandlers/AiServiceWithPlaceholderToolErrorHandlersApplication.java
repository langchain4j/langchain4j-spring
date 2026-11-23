package dev.langchain4j.service.spring.mode.explicit.placeholderToolErrorHandlers;

import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithPlaceholderToolErrorHandlersApplication {

    static final String CONFIGURED_EXECUTION_ERROR_HANDLER_BEAN_NAME = "configuredToolExecutionErrorHandler";
    static final String CONFIGURED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME = "configuredToolArgumentsErrorHandler";

    static final String NOT_WIRED = "This handler was not wired.";

    @Bean(CONFIGURED_EXECUTION_ERROR_HANDLER_BEAN_NAME)
    ToolExecutionErrorHandler configuredToolExecutionErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text("The order service is unavailable.");
    }

    @Bean
    ToolExecutionErrorHandler anotherToolExecutionErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text(NOT_WIRED);
    }

    @Bean(CONFIGURED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME)
    ToolArgumentsErrorHandler configuredToolArgumentsErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text("The order ID could not be read.");
    }

    @Bean
    ToolArgumentsErrorHandler anotherToolArgumentsErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text(NOT_WIRED);
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithPlaceholderToolErrorHandlersApplication.class, args);
    }
}
