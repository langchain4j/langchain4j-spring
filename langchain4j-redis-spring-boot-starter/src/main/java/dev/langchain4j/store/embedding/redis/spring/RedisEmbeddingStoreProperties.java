package dev.langchain4j.store.embedding.redis.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = RedisEmbeddingStoreProperties.CONFIG_PREFIX)
public class RedisEmbeddingStoreProperties {

    static final String CONFIG_PREFIX = "langchain4j.redis";

    private String host;
    private Integer port;
    private String user;
    private String password;
    private String indexName;
    private String prefix;
    private Integer dimension;
    private List<String> metadataKeys;

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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public List<String> getMetadataKeys() {
        return metadataKeys;
    }

    public void setMetadataKeys(List<String> metadataKeys) {
        this.metadataKeys = metadataKeys;
    }
}
