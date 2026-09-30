package dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers;

import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithExplicitToolErrorHandlersApplication {

    static final String WIRED_EXECUTION_ERROR_HANDLER_BEAN_NAME = "wiredToolExecutionErrorHandler";
    static final String WIRED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME = "wiredToolArgumentsErrorHandler";

    static final String NOT_WIRED = "This handler was not wired.";

    @Bean(WIRED_EXECUTION_ERROR_HANDLER_BEAN_NAME)
    ToolExecutionErrorHandler wiredToolExecutionErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text("The order service is unavailable.");
    }

    @Bean
    ToolExecutionErrorHandler anotherToolExecutionErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text(NOT_WIRED);
    }

    @Bean(WIRED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME)
    ToolArgumentsErrorHandler wiredToolArgumentsErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text("The order ID could not be read.");
    }

    @Bean
    ToolArgumentsErrorHandler anotherToolArgumentsErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text(NOT_WIRED);
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithExplicitToolErrorHandlersApplication.class, args);
    }
}
