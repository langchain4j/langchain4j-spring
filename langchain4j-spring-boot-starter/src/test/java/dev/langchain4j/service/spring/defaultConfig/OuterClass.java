package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.service.spring.AiService;

class OuterClass {

    @AiService
    interface InnerAssistant {

        String chat(String userMessage);
    }
}
