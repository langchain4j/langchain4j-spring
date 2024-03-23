package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.service.spring.AiService;

@AiService
interface PackagePrivateAssistant {

    String chat(String userMessage);
}