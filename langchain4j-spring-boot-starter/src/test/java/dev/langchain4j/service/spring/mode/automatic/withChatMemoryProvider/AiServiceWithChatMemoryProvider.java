package dev.langchain4j.service.spring.mode.automatic.withChatMemoryProvider;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithChatMemoryProvider {

    String chat(String userMessage);
}