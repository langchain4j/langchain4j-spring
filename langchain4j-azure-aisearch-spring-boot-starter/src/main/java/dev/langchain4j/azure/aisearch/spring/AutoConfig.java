package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchContentRetriever;
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
    public AzureAiSearchContentRetriever azureAiSearchContentRetriever(Properties properties, EmbeddingModel embeddingModel) {
        return AzureAiSearchContentRetriever.builder()
                .endpoint(properties.getEndpoint())
                .apiKey(properties.getApiKey())
                .createOrUpdateIndex(properties.isCreateOrUpdateIndex())
                .embeddingModel(embeddingModel)
                .dimensions(properties.getDimensions())
                .maxResults(properties.getMaxResults())
                .minScore(properties.getMinScore())
                .queryType(properties.getQueryType())
                .build();
    }
}