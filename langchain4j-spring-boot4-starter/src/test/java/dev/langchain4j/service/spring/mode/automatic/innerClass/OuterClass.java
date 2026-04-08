package dev.langchain4j.service.spring.mode.automatic.innerClass;

import dev.langchain4j.service.spring.AiService;

class OuterClass {

    @AiService
    interface InnerAiService {

        String chat(String userMessage);
    }
}
