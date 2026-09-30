package dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
class Tools {

    @Tool("Returns the status of an order")
    String orderStatus(String orderId) {
        throw new IllegalStateException("jdbc:postgresql://db:5432/prod?password=hunter2");
    }
}
