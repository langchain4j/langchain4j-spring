package dev.langchain4j.spring;

import dev.langchain4j.rag.spring.RagAutoConfig;
import dev.langchain4j.service.spring.AiServiceScannerProcessor;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        AiServicesAutoConfig.class,
        RagAutoConfig.class,
        AiServiceScannerProcessor.class
})
public class LangChain4jAutoConfig {
}
