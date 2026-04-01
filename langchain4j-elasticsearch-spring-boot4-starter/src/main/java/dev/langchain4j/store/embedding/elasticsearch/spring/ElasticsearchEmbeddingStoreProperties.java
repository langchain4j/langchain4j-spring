package dev.langchain4j.store.embedding.elasticsearch.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = ElasticsearchEmbeddingStoreProperties.PREFIX)
public class ElasticsearchEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.elasticsearch";
    static final String DEFAULT_SERVER_URL = "https://localhost:9200";
    static final String DEFAULT_INDEX_NAME = "langchain4j-index";
    static final String DEFAULT_USERNAME = "elastic";

    private String serverUrl;
    private String apiKey;
    private String username;
    private String password;
    private String indexName;
    private Boolean checkSslCertificates;
    private String caCertificateAsBase64String;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Boolean getCheckSslCertificates() {
        return checkSslCertificates;
    }

    public void setCheckSslCertificates(Boolean checkSslCertificates) {
        this.checkSslCertificates = checkSslCertificates;
    }

    public String getCaCertificateAsBase64String() {
        return caCertificateAsBase64String;
    }

    public void setCaCertificateAsBase64String(String caCertificateAsBase64String) {
        this.caCertificateAsBase64String = caCertificateAsBase64String;
    }
}
