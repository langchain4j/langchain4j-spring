package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchQueryType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;


@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.azure.ai-search";

    @NestedConfigurationProperty
    NestedProperties contentRetriever;

    @NestedConfigurationProperty
    NestedProperties embeddingStore;

    public NestedProperties getContentRetriever() {
        return contentRetriever;
    }

    public void setContentRetriever(NestedProperties contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    public NestedProperties getEmbeddingStore() {
        return embeddingStore;
    }

    public void setEmbeddingStore(NestedProperties embeddingStore) {
        this.embeddingStore = embeddingStore;
    }

    public static class NestedProperties {
        String endpoint;
        String apiKey;
        Integer dimensions;
        Boolean createOrUpdateIndex;
        String indexName;
        Integer maxResults = 3;
        Double minScore;
        AzureAiSearchQueryType queryType;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public Integer getDimensions() {
            return dimensions;
        }

        public void setDimensions(Integer dimensions) {
            this.dimensions = dimensions;
        }

        public Boolean getCreateOrUpdateIndex() {
            return createOrUpdateIndex;
        }

        public void setCreateOrUpdateIndex(Boolean createOrUpdateIndex) {
            this.createOrUpdateIndex = createOrUpdateIndex;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public Integer getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(Integer maxResults) {
            this.maxResults = maxResults;
        }

        public Double getMinScore() {
            return minScore;
        }

        public void setMinScore(Double minScore) {
            this.minScore = minScore;
        }

        public AzureAiSearchQueryType getQueryType() {
            return queryType;
        }

        public void setQueryType(AzureAiSearchQueryType queryType) {
            this.queryType = queryType;
        }
    }
}