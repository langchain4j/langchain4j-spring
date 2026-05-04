package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithTools {

    String chat(String userMessage);
}
