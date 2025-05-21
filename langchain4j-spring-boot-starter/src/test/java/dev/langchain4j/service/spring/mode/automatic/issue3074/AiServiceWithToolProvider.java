package dev.langchain4j.service.spring.mode.automatic.issue3074;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface AiServiceWithToolProvider {
    String chat(String userMessage);
}
