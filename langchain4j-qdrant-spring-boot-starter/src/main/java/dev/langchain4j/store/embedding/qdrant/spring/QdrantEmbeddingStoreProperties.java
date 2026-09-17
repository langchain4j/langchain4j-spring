package dev.langchain4j.store.embedding.qdrant.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = QdrantEmbeddingStoreProperties.PREFIX)
public class QdrantEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.qdrant";
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 6334;
    static final boolean DEFAULT_USE_TLS = false;
    static final String DEFAULT_PAYLOAD_TEXT_KEY = "text_segment";

    private String host;
    private Integer port;
    private Boolean useTls;
    private String collectionName;
    private String payloadTextKey;
    private String apiKey;
    /** 
     * The dimension of the vectors stored in the Qdrant collection.
     * Note: in Qdrant, dimension is set at collection creation time, not on the
     * embedding store itself. This property is not used by the auto-configuration
     * but is declared here for consistency with other LangChain4j starters.
    */
    private Integer dimension;

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

    public Boolean getUseTls() {
        return useTls;
    }

    public void setUseTls(Boolean useTls) {
        this.useTls = useTls;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getPayloadTextKey() {
        return payloadTextKey;
    }

    public void setPayloadTextKey(String payloadTextKey) {
        this.payloadTextKey = payloadTextKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public Integer getDimension() { 
        return dimension; 
    }

    public void setDimension(Integer dimension) { 
        this.dimension = dimension; 
    } 
}
