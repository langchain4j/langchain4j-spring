package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchQueryType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.azure.ai-search";

    @NestedConfigurationProperty
    NestedProperties contentRetriever;

    @NestedConfigurationProperty
    NestedProperties embeddingStore;

    @Getter
    @Setter
    public static class NestedProperties {
        String endpoint;
        String apiKey;
        Integer dimensions;
        Boolean createOrUpdateIndex;
        String indexName;
        Integer maxResults = 3;
        Double minScore;
        AzureAiSearchQueryType queryType;
    }
}