package dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
import static dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers.AiServiceWithExplicitToolErrorHandlersApplication.WIRED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME;
import static dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers.AiServiceWithExplicitToolErrorHandlersApplication.WIRED_EXECUTION_ERROR_HANDLER_BEAN_NAME;

@AiService(
        wiringMode = EXPLICIT,
        chatModel = "chatModel",
        tools = "tools",
        toolExecutionErrorHandler = WIRED_EXECUTION_ERROR_HANDLER_BEAN_NAME,
        toolArgumentsErrorHandler = WIRED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME)
interface AiServiceWithExplicitToolErrorHandlers {

    String chat(String userMessage);
}
