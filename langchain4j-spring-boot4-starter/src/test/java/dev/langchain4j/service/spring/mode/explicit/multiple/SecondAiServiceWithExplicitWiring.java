package dev.langchain4j.service.spring.mode.explicit.multiple;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
import static dev.langchain4j.service.spring.mode.explicit.multiple.MultipleAiServicesApplication.CHAT_MODEL_BEAN_NAME;

@AiService(wiringMode = EXPLICIT, chatModel = CHAT_MODEL_BEAN_NAME)
interface SecondAiServiceWithExplicitWiring extends BaseAiService {

    @Override
    String chat(String userMessage);
}