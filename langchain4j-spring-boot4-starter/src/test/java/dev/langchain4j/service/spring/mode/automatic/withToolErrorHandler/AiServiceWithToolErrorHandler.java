package dev.langchain4j.service.spring.mode.automatic.withToolErrorHandler;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithToolErrorHandler {

    String chat(String userMessage);
}
