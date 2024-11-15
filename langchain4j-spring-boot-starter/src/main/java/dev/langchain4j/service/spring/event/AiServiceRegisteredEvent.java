package dev.langchain4j.service.spring.event;

import dev.langchain4j.agent.tool.ToolSpecification;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class AiServiceRegisteredEvent extends ApplicationEvent {

    private final Class<?> aiServiceClass;
    private final List<ToolSpecification> toolSpecifications;

    public AiServiceRegisteredEvent(Object source, Class<?> aiServiceClass, List<ToolSpecification> toolSpecifications) {
        super(source);
        this.aiServiceClass = aiServiceClass;
        this.toolSpecifications = toolSpecifications;
    }

    public Class<?> getAiServiceClass() {
        return aiServiceClass;
    }

    public List<ToolSpecification> getToolSpecifications() {
        return toolSpecifications;
    }
}