package dev.langchain4j.service.spring.mode.explicit.tools;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel", tools = {"tools1"})
public interface AiServiceWithExplicitTools {

    String chat(String userMessage);
}
