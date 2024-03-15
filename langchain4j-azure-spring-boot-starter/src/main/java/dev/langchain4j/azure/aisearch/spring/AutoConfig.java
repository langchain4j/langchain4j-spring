package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.azure.aisearch.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".api-key")
    public AzureAiSearchEmbeddingStore azureAiSearchEmbeddingStore(Properties properties) {
        return AzureAiSearchEmbeddingStore.builder()
                .endpoint(properties.getEndpoint())
                .apiKey(properties.getApiKey())
                .dimensions(properties.getDimensions())
                .setupIndex(properties.isSetupIndex())
                .build();
    }
}