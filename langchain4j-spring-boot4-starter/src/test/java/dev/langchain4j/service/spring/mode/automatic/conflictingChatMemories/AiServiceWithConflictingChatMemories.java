package dev.langchain4j.service.spring.mode.automatic.conflictingChatMemories;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithConflictingChatMemories {

    String chat(String userMessage);
}