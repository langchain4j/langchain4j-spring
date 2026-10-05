package dev.langchain4j.store.embedding.azure.documentdb.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Azure DocumentDB embedding store.
 *
 * @see AzureDocumentDbEmbeddingStoreAutoConfiguration
 */
@ConfigurationProperties(prefix = AzureDocumentDbEmbeddingStoreProperties.PREFIX)
public class AzureDocumentDbEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.azure.documentdb";

    /**
     * MongoDB connection string for Azure DocumentDB. Required unless a MongoClient bean is provided.
     * A user-provided MongoClient takes precedence.
     */
    private String connectionString;

    /**
     * Required database name.
     */
    private String databaseName;

    /**
     * Required collection name.
     */
    private String collectionName;

    /**
     * Vector index name. Defaults to defaultIndexAzureCosmos for migration compatibility.
     */
    private String indexName;

    /**
     * Application name for an internally created MongoClient. Defaults to LangChain4j.
     */
    private String applicationName;

    /**
     * Whether to create the vector index if it is missing. Defaults to false.
     */
    private Boolean createIndex;

    /**
     * Required vector index type: vector-ivf or vector-hnsw, including when create-index is false.
     */
    private String kind;

    /**
     * Number of clusters for an IVF index. Defaults to 1.
     */
    private Integer numLists;

    /**
     * Positive embedding dimensions. Required when create-index is true, unless inferred from an
     * EmbeddingModel bean. There is no default. An explicit value takes precedence over the model.
     */
    private Integer dimensions;

    /**
     * Maximum connections per layer for an HNSW index. Defaults to 16.
     */
    private Integer m;

    /**
     * Candidate list size for HNSW index construction. Defaults to 64.
     */
    private Integer efConstruction;

    /**
     * Candidate list size for HNSW search. Defaults to 40.
     */
    private Integer efSearch;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Boolean getCreateIndex() {
        return createIndex;
    }

    public void setCreateIndex(Boolean createIndex) {
        this.createIndex = createIndex;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getNumLists() {
        return numLists;
    }

    public void setNumLists(Integer numLists) {
        this.numLists = numLists;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    public Integer getM() {
        return m;
    }

    public void setM(Integer m) {
        this.m = m;
    }

    public Integer getEfConstruction() {
        return efConstruction;
    }

    public void setEfConstruction(Integer efConstruction) {
        this.efConstruction = efConstruction;
    }

    public Integer getEfSearch() {
        return efSearch;
    }

    public void setEfSearch(Integer efSearch) {
        this.efSearch = efSearch;
    }
}
