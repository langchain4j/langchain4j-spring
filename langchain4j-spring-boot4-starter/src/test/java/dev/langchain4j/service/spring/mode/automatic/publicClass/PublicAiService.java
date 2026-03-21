package dev.langchain4j.service.spring.mode.automatic.publicClass;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface PublicAiService {

    String chat(String userMessage);
}