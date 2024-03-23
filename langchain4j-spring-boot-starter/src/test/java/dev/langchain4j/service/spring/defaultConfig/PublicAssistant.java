package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface PublicAssistant {

    String chat(String userMessage);
}