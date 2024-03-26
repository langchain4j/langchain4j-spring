package dev.langchain4j.service.spring.mode.explicit.chatModel;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
import static dev.langchain4j.service.spring.mode.explicit.chatModel.AiServiceWithExplicitChatModelApplication.CHAT_MODEL_BEAN_NAME;

@AiService(wiringMode = EXPLICIT, chatModel = CHAT_MODEL_BEAN_NAME)
interface AiServiceWithExplicitChatModel {

    String chat(String userMessage);
}