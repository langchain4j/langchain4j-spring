package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface StreamingAssistant {

    TokenStream chat(String userMessage);
}