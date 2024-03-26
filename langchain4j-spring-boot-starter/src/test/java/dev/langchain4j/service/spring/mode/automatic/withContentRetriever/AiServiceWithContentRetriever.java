package dev.langchain4j.service.spring.mode.automatic.withContentRetriever;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithContentRetriever {

    String chat(String userMessage);
}