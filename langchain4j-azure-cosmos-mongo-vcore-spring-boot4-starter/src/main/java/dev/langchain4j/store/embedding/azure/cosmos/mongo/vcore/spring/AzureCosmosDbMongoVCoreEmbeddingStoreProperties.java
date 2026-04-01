package dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Azure Cosmos DB for MongoDB vCore Embedding Store.
 * <p>
 * Properties are prefixed with {@code langchain4j.azure.cosmos-mongo-vcore}.
 * </p>
 * <p>
 * Example configuration:
 * <pre>
 * langchain4j.azure.cosmos-mongo-vcore.connection-string=mongodb+srv://...
 * langchain4j.azure.cosmos-mongo-vcore.database-name=mydb
 * langchain4j.azure.cosmos-mongo-vcore.collection-name=embeddings
 * langchain4j.azure.cosmos-mongo-vcore.index-name=vectorIndex
 * langchain4j.azure.cosmos-mongo-vcore.create-index=true
 * langchain4j.azure.cosmos-mongo-vcore.dimensions=1536
 * </pre>
 *
 * @see AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration
 */
@ConfigurationProperties(prefix = AzureCosmosDbMongoVCoreEmbeddingStoreProperties.PREFIX)
public class AzureCosmosDbMongoVCoreEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.azure.cosmos-mongo-vcore";

    /**
     * The MongoDB connection string for Azure Cosmos DB for MongoDB vCore.
     */
    private String connectionString;

    /**
     * The name of the database to use.
     */
    private String databaseName;

    /**
     * The name of the collection to store embeddings.
     */
    private String collectionName;

    /**
     * The name of the vector index.
     */
    private String indexName;

    /**
     * The application name to use in the connection.
     */
    private String applicationName;

    /**
     * Whether to create the vector index if it doesn't exist.
     */
    private Boolean createIndex;

    /**
     * The kind of vector index to create (e.g., "vector-ivf", "vector-hnsw").
     */
    private String kind;

    /**
     * The number of clusters for IVF index. Only applicable when kind is "vector-ivf".
     */
    private Integer numLists;

    /**
     * The number of dimensions for the embedding vectors.
     */
    private Integer dimensions;

    /**
     * The max number of connections per layer for HNSW index. Only applicable when kind is "vector-hnsw".
     */
    private Integer m;

    /**
     * The size of the dynamic candidate list during HNSW index construction. Only applicable when kind is "vector-hnsw".
     */
    private Integer efConstruction;

    /**
     * The size of the dynamic candidate list during HNSW search. Only applicable when kind is "vector-hnsw".
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
