package dev.langchain4j.service.spring.mode.automatic.scanPackages.aiService;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface ScanPackageAiService {

    String chat(String userMessage);
}