package dev.langchain4j.service.spring.mode.automatic.withModerationModel;

import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithModerationModel {

    @Moderate
    String chat(String userMessage);

}
