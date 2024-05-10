package dev.langchain4j.service.spring.mode.automatic.streaming;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.spring.AiService;

@AiService
interface StreamingAiService {

    TokenStream chat(String userMessage);
}