package dev.langchain4j.store.embedding.pgvector.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = PgVectorEmbeddingStoreProperties.PREFIX)
public class PgVectorEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.pgvector";

    /**
     * The database table.
     */
    private String table;

    /**
     * The vector dimension.
     */
    private Integer dimension;

    /**
     * Should create table automatically, default value is <code>false</code>.
     */
    private Boolean createTable;

    /**
     * Should use <a href="https://github.com/pgvector/pgvector#ivfflat">IVFFlat</a> index.
     */
    private Boolean useIndex;

    /**
     * The IVFFlat number of lists.
     */
    private Integer indexListSize;

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

    public Boolean getCreateTable() {
        return createTable;
    }

    public void setCreateTable(Boolean createTable) {
        this.createTable = createTable;
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
}
