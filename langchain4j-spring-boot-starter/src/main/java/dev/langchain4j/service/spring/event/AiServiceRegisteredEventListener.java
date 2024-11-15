package dev.langchain4j.service.spring.event;

import org.springframework.context.ApplicationListener;

public interface AiServiceRegisteredEventListener extends ApplicationListener<AiServiceRegisteredEvent> {
}
