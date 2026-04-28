package dev.langchain4j.store.embedding.milvus.spring;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.milvus.common.clientenum.ConsistencyLevelEnum.STRONG;

@ConfigurationProperties(prefix = MilvusEmbeddingStoreProperties.PREFIX)
public class MilvusEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.milvus";
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 19530;
    static final String DEFAULT_COLLECTION_NAME = "langchain4j_collection";
    static final ConsistencyLevelEnum DEFAULT_CONSISTENCY_LEVEL = STRONG;

    private String host;
    private Integer port;
    private String collectionName;
    private Integer dimension;
    private IndexType indexType;
    private MetricType metricType;
    private String uri;
    private String token;
    private String username;
    private String password;
    private ConsistencyLevelEnum consistencyLevel;
    private Boolean retrieveEmbeddingsOnSearch;
    private Boolean autoFlushOnInsert;
    private String databaseName;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConsistencyLevelEnum getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(ConsistencyLevelEnum consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public Boolean getRetrieveEmbeddingsOnSearch() {
        return retrieveEmbeddingsOnSearch;
    }

    public void setRetrieveEmbeddingsOnSearch(Boolean retrieveEmbeddingsOnSearch) {
        this.retrieveEmbeddingsOnSearch = retrieveEmbeddingsOnSearch;
    }

    public Boolean getAutoFlushOnInsert() {
        return autoFlushOnInsert;
    }

    public void setAutoFlushOnInsert(Boolean autoFlushOnInsert) {
        this.autoFlushOnInsert = autoFlushOnInsert;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
