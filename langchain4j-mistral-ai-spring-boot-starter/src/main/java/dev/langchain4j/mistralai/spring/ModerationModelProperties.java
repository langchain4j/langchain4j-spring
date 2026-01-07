package dev.langchain4j.mistralai.spring;

import java.time.Duration;

class ModerationModelProperties {

    String baseUrl;
    String apiKey;
    String modelName;
    Duration timeout;
    Boolean logRequests;
    Boolean logResponses;
    Integer maxRetries;

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getModelName() {
        return this.modelName;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public Boolean getLogRequests() {
        return this.logRequests;
    }

    public Boolean getLogResponses() {
        return this.logResponses;
    }

    public Integer getMaxRetries() {
        return this.maxRetries;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void setLogRequests(Boolean logRequests) {
        this.logRequests = logRequests;
    }

    public void setLogResponses(Boolean logResponses) {
        this.logResponses = logResponses;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
