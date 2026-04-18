package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = ChromaEmbeddingStoreProperties.PREFIX)
public class ChromaEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.chroma";
    static final String DEFAULT_BASE_URL = "http://localhost:8000";
    static final String DEFAULT_COLLECTION_NAME = "default";
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private String baseUrl;
    private String collectionName;
    private String tenantName;
    private String databaseName;
    private Duration timeout;
    private Boolean logRequests;
    private Boolean logResponses;
    private ChromaApiVersion apiVersion;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }

    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }

    public Boolean getLogRequests() { return logRequests; }
    public void setLogRequests(Boolean logRequests) { this.logRequests = logRequests; }

    public Boolean getLogResponses() { return logResponses; }
    public void setLogResponses(Boolean logResponses) { this.logResponses = logResponses; }

    public ChromaApiVersion getApiVersion() { return apiVersion; }
    public void setApiVersion(ChromaApiVersion apiVersion) { this.apiVersion = apiVersion; }
}
