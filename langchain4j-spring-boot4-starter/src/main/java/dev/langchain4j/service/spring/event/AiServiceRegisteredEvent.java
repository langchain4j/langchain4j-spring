package dev.langchain4j.service.spring.event;

import dev.langchain4j.agent.tool.ToolSpecification;
import org.springframework.context.ApplicationEvent;

import java.util.List;

import static dev.langchain4j.internal.Utils.copyIfNotNull;

public class AiServiceRegisteredEvent extends ApplicationEvent {

    private final Class<?> aiServiceClass;
    private final List<ToolSpecification> toolSpecifications;

    public AiServiceRegisteredEvent(Object source, Class<?> aiServiceClass, List<ToolSpecification> toolSpecifications) {
        super(source);
        this.aiServiceClass = aiServiceClass;
        this.toolSpecifications = copyIfNotNull(toolSpecifications);
    }

    public Class<?> aiServiceClass() {
        return aiServiceClass;
    }

    public List<ToolSpecification> toolSpecifications() {
        return toolSpecifications;
    }
}