package dev.langchain4j.store.embedding.pgvector.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = PgVectorEmbeddingStoreProperties.PREFIX)
public class PgVectorEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.pgvector";
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 5432;
    static final String DEFAULT_DATABASE = "langchain4j_database";
    static final String DEFAULT_TABLE = "langchain4j_table";

    private String host;
    private Integer port;
    private String database;
    private String table;
    private Integer dimension;
    private String user;
    private String password;

    private Boolean useIndex;
    private Integer indexListSize;
    private Boolean createTable;
    private Boolean dropTableFirst;

    @NestedConfigurationProperty
    private MetadataStorageConfigProperties metadataStorageConfig;
}
