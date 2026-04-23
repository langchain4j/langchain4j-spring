package dev.langchain4j.store.embedding.pgvector.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for {@link dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore}
 * when using the Spring Boot 4 starter.
 */
@ConfigurationProperties(prefix = PgVectorEmbeddingStoreProperties.PREFIX)
public class PgVectorEmbeddingStoreProperties {

    static final String PREFIX = "spring.langchain4j.pgvector";

    private String host = "localhost";
    private Integer port = 5432;
    private String database;
    private String user;
    private String password;
    private Boolean ssl = false;
    private String tableName = "embeddings";
    private Integer dimension;
    private Boolean useIndex = false;
    private Integer indexListSize;
    private Boolean createTable = true;
    private Boolean dropTableFirst = false;
    private Boolean skipCreateVectorExtension = false;

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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public Boolean getUseIndex() {
        return useIndex;
    }

    public void setUseIndex(Boolean useIndex) {
        this.useIndex = useIndex;
    }

    public Integer getIndexListSize() {
        return indexListSize;
    }

    public void setIndexListSize(Integer indexListSize) {
        this.indexListSize = indexListSize;
    }

    public Boolean getCreateTable() {
        return createTable;
    }

    public void setCreateTable(Boolean createTable) {
        this.createTable = createTable;
    }

    public Boolean getDropTableFirst() {
        return dropTableFirst;
    }

    public void setDropTableFirst(Boolean dropTableFirst) {
        this.dropTableFirst = dropTableFirst;
    }

    public Boolean getSkipCreateVectorExtension() {
        return skipCreateVectorExtension;
    }

    public void setSkipCreateVectorExtension(Boolean skipCreateVectorExtension) {
        this.skipCreateVectorExtension = skipCreateVectorExtension;
    }
}
