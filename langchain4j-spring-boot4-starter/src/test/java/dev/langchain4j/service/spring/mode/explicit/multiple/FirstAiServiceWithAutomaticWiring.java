package dev.langchain4j.service.spring.mode.explicit.multiple;

import dev.langchain4j.service.spring.AiService;

@AiService // wiringMode = AUTOMATIC is default
interface FirstAiServiceWithAutomaticWiring extends BaseAiService {

    @Override
    String chat(String userMessage);
}