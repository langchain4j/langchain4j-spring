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
}
