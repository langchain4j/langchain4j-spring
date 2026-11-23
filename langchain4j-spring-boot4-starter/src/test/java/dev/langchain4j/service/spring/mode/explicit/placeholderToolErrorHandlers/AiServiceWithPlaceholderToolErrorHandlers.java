package dev.langchain4j.service.spring.mode.explicit.placeholderToolErrorHandlers;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        chatModel = "chatModel",
        tools = "tools",
        toolExecutionErrorHandler = "${my.tool-execution-error-handler.name}",
        toolArgumentsErrorHandler = "${my.tool-arguments-error-handler.name}")
interface AiServiceWithPlaceholderToolErrorHandlers {

    String chat(String userMessage);
}
