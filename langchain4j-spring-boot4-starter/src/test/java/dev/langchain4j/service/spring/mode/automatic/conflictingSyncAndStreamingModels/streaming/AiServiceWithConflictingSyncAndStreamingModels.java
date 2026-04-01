package dev.langchain4j.service.spring.mode.automatic.conflictingSyncAndStreamingModels.streaming;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithConflictingSyncAndStreamingModels {

    TokenStream chat(String userMessage);
}