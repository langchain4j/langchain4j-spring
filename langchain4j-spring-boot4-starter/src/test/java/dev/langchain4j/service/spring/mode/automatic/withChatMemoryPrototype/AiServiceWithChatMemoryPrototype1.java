package dev.langchain4j.service.spring.mode.automatic.withChatMemoryPrototype;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithChatMemoryPrototype1 {

    String chat(String userMessage);
}