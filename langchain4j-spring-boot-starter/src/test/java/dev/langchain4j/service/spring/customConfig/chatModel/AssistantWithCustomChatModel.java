package dev.langchain4j.service.spring.customConfig.chatModel;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.customConfig.chatModel.TestApplicationWithCustomChatModel.CUSTOM_CHAT_MODEL_BEAN_NAME;

@AiService(chatModel = CUSTOM_CHAT_MODEL_BEAN_NAME)
interface AssistantWithCustomChatModel {

    String chat(String userMessage);
}