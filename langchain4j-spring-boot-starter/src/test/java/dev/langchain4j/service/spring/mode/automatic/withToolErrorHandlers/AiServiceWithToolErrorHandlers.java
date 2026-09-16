package dev.langchain4j.service.spring.mode.automatic.withToolErrorHandlers;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithToolErrorHandlers {

    String chat(String userMessage);
}
