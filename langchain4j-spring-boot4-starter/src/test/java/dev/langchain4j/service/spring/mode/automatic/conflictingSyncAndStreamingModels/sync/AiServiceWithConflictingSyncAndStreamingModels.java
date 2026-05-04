package dev.langchain4j.service.spring.mode.automatic.conflictingSyncAndStreamingModels.sync;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithConflictingSyncAndStreamingModels {

    String chat(String userMessage);
}