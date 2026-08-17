package dev.langchain4j.service.spring.mode.autoConfiguredModel;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AutoConfiguredModelAiService {

    String chat(String userMessage);
}
