package dev.langchain4j.service.spring.customConfig.multipleChatModels;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AssistantWithMultipleChatModels {

    String chat(String userMessage);
}