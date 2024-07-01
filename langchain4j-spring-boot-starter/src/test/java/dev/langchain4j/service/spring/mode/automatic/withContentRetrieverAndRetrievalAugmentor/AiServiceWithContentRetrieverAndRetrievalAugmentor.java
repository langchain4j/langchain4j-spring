package dev.langchain4j.service.spring.mode.automatic.withContentRetrieverAndRetrievalAugmentor;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithContentRetrieverAndRetrievalAugmentor {

    String chat(String userMessage);
}