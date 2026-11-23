package dev.langchain4j.service.spring.mode.explicit.placeholder;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "${my.chat-model.name}", tools = {"${my.tool.name}"})
interface AiServiceWithPlaceholderTools {

    String chat(String userMessage);
}
