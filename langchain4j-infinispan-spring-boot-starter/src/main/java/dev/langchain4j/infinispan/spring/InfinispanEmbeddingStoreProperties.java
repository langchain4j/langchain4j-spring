package dev.langchain4j.infinispan.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = InfinispanEmbeddingStoreProperties.PREFIX)
public class InfinispanEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.infinispan";

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String cacheName;
    private Integer dimension;
    private Integer distance;
    private String similarity;
    private String cacheConfig;

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

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(String cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

}
