package dev.langchain4j.store.embedding.redis.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = RedisEmbeddingStoreProperties.PREFIX)
@Getter
@Setter
public class RedisEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.redis";

    private String host;
    private Integer port;
    private String user;
    private String password;
    private String indexName;
    private Integer dimension;
    private List<String> metadataKeys = new ArrayList<>();
}
