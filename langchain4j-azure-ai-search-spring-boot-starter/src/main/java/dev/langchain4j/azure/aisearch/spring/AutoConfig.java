package dev.langchain4j.azure.aisearch.spring;

import com.azure.search.documents.indexes.models.SearchIndex;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchContentRetriever;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import static dev.langchain4j.azure.aisearch.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {
    @Bean
    @ConditionalOnProperty(PREFIX + ".content-retriver.api-key")
    public AzureAiSearchContentRetriever azureAiSearchContentRetriever(Properties properties, @Nullable EmbeddingModel embeddingModel, @Nullable SearchIndex index) {
        Properties.NestedProperties nestedProperties = properties.getContentRetriver();
        return AzureAiSearchContentRetriever.builder()
                .endpoint(nestedProperties.getEndpoint())
                .apiKey(nestedProperties.getApiKey())
                .createOrUpdateIndex(nestedProperties.isCreateOrUpdateIndex())
                .embeddingModel(embeddingModel)
                .dimensions(nestedProperties.getDimensions())
                .index(index)
                .maxResults(nestedProperties.getMaxResults())
                .minScore(nestedProperties.getMinScore())
                .queryType(nestedProperties.getQueryType())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-store.api-key")
    public AzureAiSearchEmbeddingStore azureAiSearchEmbeddingStore(Properties properties, @Nullable EmbeddingModel embeddingModel, @Nullable SearchIndex index) {
        Properties.NestedProperties nestedProperties = properties.getEmbeddingStore();
        return AzureAiSearchEmbeddingStore.builder()
                .endpoint(nestedProperties.getEndpoint())
                .apiKey(nestedProperties.getApiKey())
                .createOrUpdateIndex(nestedProperties.isCreateOrUpdateIndex())
                .dimensions(nestedProperties.getDimensions())
                .index(index)
                .build();
    }
}