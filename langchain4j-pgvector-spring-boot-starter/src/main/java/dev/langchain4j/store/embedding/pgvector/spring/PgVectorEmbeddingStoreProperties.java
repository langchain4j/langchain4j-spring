package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.PREFIX;

@ConfigurationProperties(prefix = PREFIX)
public class PgVectorEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.pgvector";
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 5432;
    static final String DEFAULT_TABLE = "langchain4j_embedding";
    static final String DEFAULT_DATABASE = "postgres";

    private String host;
    private Integer port;
    private String user;
    private String password;
    private String database;
    private String table;
    private Integer dimension;
    private Boolean useIndex;
    private Integer indexListSize;
    private Boolean createTable;
    private Boolean dropTableFirst;
    private Boolean skipCreateVectorExtension;
    private PgVectorEmbeddingStore.SearchMode searchMode;
    private String textSearchConfig;
    private Integer rrfK;

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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
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

    public PgVectorEmbeddingStore.SearchMode getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(PgVectorEmbeddingStore.SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public String getTextSearchConfig() {
        return textSearchConfig;
    }

    public void setTextSearchConfig(String textSearchConfig) {
        this.textSearchConfig = textSearchConfig;
    }

    public Integer getRrfK() {
        return rrfK;
    }

    public void setRrfK(Integer rrfK) {
        this.rrfK = rrfK;
    }

    String host() {
        return host;
    }

    Integer port() {
        return port;
    }

    String user() {
        return user;
    }

    String password() {
        return password;
    }

    String database() {
        return database;
    }

    String table() {
        return table;
    }

    Integer dimension() {
        return dimension;
    }

    Boolean useIndex() {
        return useIndex;
    }

    Integer indexListSize() {
        return indexListSize;
    }

    Boolean createTable() {
        return createTable;
    }

    Boolean dropTableFirst() {
        return dropTableFirst;
    }

    Boolean skipCreateVectorExtension() {
        return skipCreateVectorExtension;
    }

    PgVectorEmbeddingStore.SearchMode searchMode() {
        return searchMode;
    }

    String textSearchConfig() {
        return textSearchConfig;
    }

    Integer rrfK() {
        return rrfK;
    }
}
