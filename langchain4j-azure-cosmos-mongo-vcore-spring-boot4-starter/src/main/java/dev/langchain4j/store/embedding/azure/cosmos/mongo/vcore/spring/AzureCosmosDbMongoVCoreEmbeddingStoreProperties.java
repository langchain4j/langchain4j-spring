package dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * Configuration properties for the Azure DocumentDB (with MongoDB compatibility) Embedding Store.
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
 * @deprecated Azure Cosmos DB for MongoDB vCore has been renamed to Azure DocumentDB.
 * Use {@code dev.langchain4j.store.embedding.azure.documentdb.spring.AzureDocumentDbEmbeddingStoreProperties}
 * from {@code langchain4j-azure-documentdb-spring-boot4-starter} instead, with properties under {@code langchain4j.azure.documentdb}.
 */
@Deprecated(forRemoval = true)
@ConfigurationProperties(prefix = AzureCosmosDbMongoVCoreEmbeddingStoreProperties.PREFIX)
public class AzureCosmosDbMongoVCoreEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.azure.cosmos-mongo-vcore";

    /**
     * The MongoDB connection string for Azure DocumentDB.
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

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.connection-string")
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.database-name")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.collection-name")
    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.index-name")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.application-name")
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.create-index")
    public Boolean getCreateIndex() {
        return createIndex;
    }

    public void setCreateIndex(Boolean createIndex) {
        this.createIndex = createIndex;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.kind")
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.num-lists")
    public Integer getNumLists() {
        return numLists;
    }

    public void setNumLists(Integer numLists) {
        this.numLists = numLists;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.dimensions")
    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.m")
    public Integer getM() {
        return m;
    }

    public void setM(Integer m) {
        this.m = m;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.ef-construction")
    public Integer getEfConstruction() {
        return efConstruction;
    }

    public void setEfConstruction(Integer efConstruction) {
        this.efConstruction = efConstruction;
    }

    @DeprecatedConfigurationProperty(reason = "Azure Cosmos DB for MongoDB vCore was renamed to Azure DocumentDB",
            replacement = "langchain4j.azure.documentdb.ef-search")
    public Integer getEfSearch() {
        return efSearch;
    }

    public void setEfSearch(Integer efSearch) {
        this.efSearch = efSearch;
    }
}
