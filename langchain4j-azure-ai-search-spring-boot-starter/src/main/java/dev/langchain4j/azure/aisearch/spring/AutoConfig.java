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
    @ConditionalOnProperty(PREFIX + ".content-retriever.api-key")
    public AzureAiSearchContentRetriever azureAiSearchContentRetriever(Properties properties, @Nullable EmbeddingModel embeddingModel, @Nullable SearchIndex index) {
        Properties.NestedProperties nestedProperties = properties.contentRetriever();
        return AzureAiSearchContentRetriever.builder()
                .endpoint(nestedProperties.endpoint())
                .apiKey(nestedProperties.apiKey())
                .createOrUpdateIndex(nestedProperties.createOrUpdateIndex())
                .embeddingModel(embeddingModel)
                .dimensions(nestedProperties.dimensions())
                .index(index)
                .maxResults(nestedProperties.maxResults())
                .minScore(nestedProperties.minScore())
                .queryType(nestedProperties.queryType())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-store.api-key")
    public AzureAiSearchEmbeddingStore azureAiSearchEmbeddingStore(Properties properties, @Nullable EmbeddingModel embeddingModel, @Nullable SearchIndex index) {
         Properties.NestedProperties nestedProperties = properties.embeddingStore();
        return AzureAiSearchEmbeddingStore.builder()
                .endpoint(nestedProperties.endpoint())
                .apiKey(nestedProperties.apiKey())
                .createOrUpdateIndex(nestedProperties.createOrUpdateIndex())
                .dimensions(nestedProperties.dimensions())
                .index(index)
                .build();
    }
}