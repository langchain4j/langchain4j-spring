package dev.langchain4j.store.embedding.milvus.spring;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.milvus.common.clientenum.ConsistencyLevelEnum.STRONG;

@Getter
@Setter
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
}
