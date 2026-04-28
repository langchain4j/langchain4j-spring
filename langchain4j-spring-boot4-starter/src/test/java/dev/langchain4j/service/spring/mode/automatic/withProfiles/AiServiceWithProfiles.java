package dev.langchain4j.service.spring.mode.automatic.withProfiles;

import dev.langchain4j.service.spring.AiService;
import org.springframework.context.annotation.Profile;

@AiService
@Profile("!test")
public interface AiServiceWithProfiles {

    String chat(String userMessage);
}
