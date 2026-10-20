package dev.langchain4j.service.spring.mode.wiringLog;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AutomaticAssistant {

    String chat(String userMessage);
}
