package dev.langchain4j.spring;

import dev.langchain4j.rag.spring.RagAutoConfiguration;
import dev.langchain4j.service.spring.AiServiceScannerProcessor;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        AiServicesAutoConfiguration.class,
        RagAutoConfiguration.class,
        AiServiceScannerProcessor.class
})
public class LangChain4jAutoConfiguration {
}
