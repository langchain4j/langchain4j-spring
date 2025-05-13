package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchQueryType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = Properties.PREFIX)
public record Properties(
    @NestedConfigurationProperty
    NestedProperties contentRetriever,
    @NestedConfigurationProperty
    NestedProperties embeddingStore
    ) {

    static final String PREFIX = "langchain4j.azure.ai-search";

    public static record NestedProperties(
        String endpoint,
        String apiKey,
        int dimensions,
        Boolean createOrUpdateIndex,
        String indexName,
        @DefaultValue("3")
        Integer maxResults,
        double minScore,
        AzureAiSearchQueryType queryType
        ) {

    }
}
