package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchQueryType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.azure.ai-search";

    String endpoint;
    String apiKey;
    int dimensions;
    boolean createOrUpdateIndex;
    int maxResults = 1;
    double minScore = 0.6;
    AzureAiSearchQueryType queryType;
}
