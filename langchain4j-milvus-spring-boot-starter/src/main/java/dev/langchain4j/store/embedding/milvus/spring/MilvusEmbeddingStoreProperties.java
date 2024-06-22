package dev.langchain4j.store.embedding.milvus.spring;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = MilvusEmbeddingStoreProperties.PREFIX)
public class MilvusEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.milvus";

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
