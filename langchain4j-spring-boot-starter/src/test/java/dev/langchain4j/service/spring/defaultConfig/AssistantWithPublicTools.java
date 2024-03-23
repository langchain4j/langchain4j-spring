package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.service.spring.AiService;

@AiService(tools = PublicTools.class)
public interface AssistantWithPublicTools {

    String chat(String userMessage);
}