package dev.langchain4j.service.spring.mode.automatic.withRetrievalAugmentor;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithRetrievalAugmentor {

    String chat(String userMessage);
}