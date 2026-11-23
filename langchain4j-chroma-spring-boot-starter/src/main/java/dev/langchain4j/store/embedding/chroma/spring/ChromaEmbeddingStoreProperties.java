package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static dev.langchain4j.store.embedding.chroma.ChromaApiVersion.V1;
import static dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreProperties.PREFIX;

@ConfigurationProperties(prefix = PREFIX)
public class ChromaEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.chroma";
    static final String DEFAULT_BASE_URL = "http://localhost:8000";
    static final String DEFAULT_COLLECTION_NAME = "default";
    static final ChromaApiVersion DEFAULT_API_VERSION = V1;

    private String baseUrl;
    private ChromaApiVersion apiVersion;
    private String tenantName;
    private String databaseName;
    private String collectionName;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ChromaApiVersion getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(ChromaApiVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    String baseUrl() {
        return baseUrl;
    }

    ChromaApiVersion apiVersion() {
        return apiVersion;
    }

    String tenantName() {
        return tenantName;
    }

    String databaseName() {
        return databaseName;
    }

    String collectionName() {
        return collectionName;
    }
}
