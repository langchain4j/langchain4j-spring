package dev.langchain4j.service.spring.mode.automatic.withTools.listener;

import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import org.springframework.stereotype.Component;
@Component
public class AiServiceRegisteredEventListener extends AbstractApplicationListener<AiServiceRegisteredEvent> {
}
