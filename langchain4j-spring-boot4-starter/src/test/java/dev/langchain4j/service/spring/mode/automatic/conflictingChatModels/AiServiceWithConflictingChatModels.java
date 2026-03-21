package dev.langchain4j.service.spring.mode.automatic.conflictingChatModels;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithConflictingChatModels {

    String chat(String userMessage);
}