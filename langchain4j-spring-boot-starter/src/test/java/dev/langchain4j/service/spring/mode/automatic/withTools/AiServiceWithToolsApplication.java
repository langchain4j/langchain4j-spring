package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
class AiServiceWithToolsApplication implements AiServiceRegisteredEventListener {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithToolsApplication.class, args);
    }

    @Override
    public void onApplicationEvent(AiServiceRegisteredEvent event) {
        Class<?> aiServiceClass = event.getAiServiceClass();
        List<ToolSpecification> toolSpecifications = event.getToolSpecifications();
        for (int i = 0; i < toolSpecifications.size(); i++) {
            System.out.printf("[%s]: [Tool-%s]: %s%n", aiServiceClass.getSimpleName(), i + 1, toolSpecifications.get(i));
        }
    }
}
