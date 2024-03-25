package dev.langchain4j.service.spring.mode.automatic.missingAnnotation;

// no @AiService annotation
interface AssistantWithMissingAnnotation {

    String chat(String userMessage);
}