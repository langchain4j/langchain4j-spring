package dev.langchain4j.service.spring.mode.automatic.withToolProvider;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithToolProvider {

    String chat(String userMessage);
}