package dev.langchain4j.service.spring.mode.automatic.withChatMemory;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithChatMemory {

    String chat(String userMessage);
}