package dev.langchain4j.service.spring.mode.automatic.packagePrivateClass;

import dev.langchain4j.service.spring.AiService;

@AiService
interface PackagePrivateAiService {

    String chat(String userMessage);
}