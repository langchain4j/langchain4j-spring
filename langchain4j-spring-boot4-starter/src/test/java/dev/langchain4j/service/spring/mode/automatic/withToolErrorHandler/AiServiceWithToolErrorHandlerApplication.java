package dev.langchain4j.service.spring.mode.automatic.withToolErrorHandler;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
class AiServiceWithToolErrorHandlerApplication {

    @Component
    static class Tools {

        @Tool("Returns the status of an order")
        String orderStatus(String orderId) {
            throw new IllegalStateException("jdbc:postgresql://db:5432/prod?password=hunter2");
        }
    }

    @Bean
    ToolExecutionErrorHandler toolExecutionErrorHandler() {
        return (error, errorContext) -> ToolErrorHandlerResult.text("The order service is unavailable.");
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithToolErrorHandlerApplication.class, args);
    }
}
