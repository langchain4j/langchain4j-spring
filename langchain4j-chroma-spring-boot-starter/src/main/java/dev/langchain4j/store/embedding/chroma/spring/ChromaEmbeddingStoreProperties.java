package dev.langchain4j.store.embedding.chroma.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = ChromaEmbeddingStoreProperties.PREFIX)
public class ChromaEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.chroma";

    private String url;
    private String apiKey;
    private String collectionName;
    private Boolean createCollection = Boolean.FALSE;
    private Integer dimensions;
    private Boolean enabled = Boolean.TRUE;
    private String apiVersion;
    private String tenantName;
    private String databaseName;
    private Boolean logRequests = Boolean.FALSE;
    private Boolean logResponses = Boolean.FALSE;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public Boolean getCreateCollection() { return createCollection; }
    public void setCreateCollection(Boolean createCollection) { this.createCollection = createCollection; }

    public Integer getDimensions() { return dimensions; }
    public void setDimensions(Integer dimensions) { this.dimensions = dimensions; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }

    public Boolean getLogRequests() { return logRequests; }
    public void setLogRequests(Boolean logRequests) { this.logRequests = logRequests; }

    public Boolean getLogResponses() { return logResponses; }
    public void setLogResponses(Boolean logResponses) { this.logResponses = logResponses; }
}