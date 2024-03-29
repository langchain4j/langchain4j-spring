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
    NestedProperties contentRetriver;

    @NestedConfigurationProperty
    NestedProperties embeddingStore;

    @Getter
    @Setter
    public static class NestedProperties {
        String endpoint;
        String apiKey;
        int dimensions;
        boolean createOrUpdateIndex;
        int maxResults = 3;
        double minScore;
        AzureAiSearchQueryType queryType;
    }
}