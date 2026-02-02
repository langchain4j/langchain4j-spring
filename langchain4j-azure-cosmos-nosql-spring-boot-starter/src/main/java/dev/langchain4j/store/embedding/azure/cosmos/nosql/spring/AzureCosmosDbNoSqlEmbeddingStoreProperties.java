package dev.langchain4j.store.embedding.azure.cosmos.nosql.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Azure Cosmos DB NoSQL Embedding Store.
 * <p>
 * Properties are prefixed with {@code langchain4j.azure.cosmos-nosql}.
 * </p>
 * <p>
 * Example configuration:
 * <pre>
 * langchain4j.azure.cosmos-nosql.endpoint=https://myaccount.documents.azure.com:443/
 * langchain4j.azure.cosmos-nosql.key=your-api-key
 * langchain4j.azure.cosmos-nosql.database-name=mydb
 * langchain4j.azure.cosmos-nosql.container-name=embeddings
 * langchain4j.azure.cosmos-nosql.dimensions=1536
 * </pre>
 *
 * @see AzureCosmosDbNoSqlEmbeddingStoreAutoConfiguration
 */
@ConfigurationProperties(prefix = AzureCosmosDbNoSqlEmbeddingStoreProperties.PREFIX)
public class AzureCosmosDbNoSqlEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.azure.cosmos-nosql";

    /**
     * The Azure Cosmos DB endpoint URL.
     */
    private String endpoint;

    /**
     * The Azure Cosmos DB API key.
     */
    private String key;

    /**
     * The name of the database to use.
     */
    private String databaseName;

    /**
     * The name of the container to store embeddings.
     */
    private String containerName;

    /**
     * The partition key path for the container (e.g., "/id").
     */
    private String partitionKeyPath;

    /**
     * The provisioned throughput for the vector store container.
     */
    private Integer vectorStoreThroughput;

    /**
     * The number of dimensions for the embedding vectors.
     * This is required for vector search configuration.
     */
    private Integer dimensions;

    /**
     * The path to the embedding field in the document. Defaults to "/embedding".
     */
    private String embeddingPath = "/embedding";

    /**
     * The distance function for vector similarity search.
     * Valid values are: COSINE, DOT_PRODUCT, EUCLIDEAN.
     * Defaults to COSINE.
     */
    private String distanceFunction = "COSINE";

    /**
     * The data type for storing embedding vectors.
     * Valid values are: FLOAT32, INT8, UINT8.
     * Defaults to FLOAT32.
     */
    private String dataType = "FLOAT32";

    /**
     * The vector index type.
     * Valid values are: FLAT, DISK_ANN, QUANTIZED_FLAT.
     * Defaults to DISK_ANN.
     */
    private String vectorIndexType = "DISK_ANN";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getPartitionKeyPath() {
        return partitionKeyPath;
    }

    public void setPartitionKeyPath(String partitionKeyPath) {
        this.partitionKeyPath = partitionKeyPath;
    }

    public Integer getVectorStoreThroughput() {
        return vectorStoreThroughput;
    }

    public void setVectorStoreThroughput(Integer vectorStoreThroughput) {
        this.vectorStoreThroughput = vectorStoreThroughput;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    public String getEmbeddingPath() {
        return embeddingPath;
    }

    public void setEmbeddingPath(String embeddingPath) {
        this.embeddingPath = embeddingPath;
    }

    public String getDistanceFunction() {
        return distanceFunction;
    }

    public void setDistanceFunction(String distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getVectorIndexType() {
        return vectorIndexType;
    }

    public void setVectorIndexType(String vectorIndexType) {
        this.vectorIndexType = vectorIndexType;
    }
}
