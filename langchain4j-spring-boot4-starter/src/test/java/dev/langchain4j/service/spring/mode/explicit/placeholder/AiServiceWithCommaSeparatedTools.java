package dev.langchain4j.service.spring.mode.explicit.placeholder;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "configuredChatModel", tools = {"configuredTool, secondTool"})
interface AiServiceWithCommaSeparatedTools {

    String chat(String userMessage);
}
