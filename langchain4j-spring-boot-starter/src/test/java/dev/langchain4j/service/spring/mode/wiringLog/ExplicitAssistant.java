package dev.langchain4j.service.spring.mode.wiringLog;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        chatModel = "${wiring-log.chat-model:chatModel}",
        tools = "${wiring-log.tools:statusTool}"
)
interface ExplicitAssistant {

    String chat(String userMessage);
}
