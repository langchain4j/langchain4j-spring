package dev.langchain4j.service.spring.mode.automatic.withStructuredOutput;

import dev.langchain4j.service.spring.AiService;

@AiService
interface AiServiceWithStructuredOutput {

    Person extractPersonFrom(String text);
}