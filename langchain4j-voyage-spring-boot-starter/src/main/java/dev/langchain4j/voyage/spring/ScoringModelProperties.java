package dev.langchain4j.voyage.spring;

import dev.langchain4j.model.voyage.VoyageScoringModelName;

import java.time.Duration;

public class ScoringModelProperties {

    private String baseUrl;
    private Duration timeout;
    private Integer maxRetries;
    private String apiKey;
    private VoyageScoringModelName modelName;
    private Integer topK;
    private Boolean returnDocuments;
    private Boolean truncation;
    private Boolean logRequests;
    private Boolean logResponses;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public VoyageScoringModelName getModelName() {
        return modelName;
    }

    public void setModelName(VoyageScoringModelName modelName) {
        this.modelName = modelName;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Boolean getReturnDocuments() {
        return returnDocuments;
    }

    public void setReturnDocuments(Boolean returnDocuments) {
        this.returnDocuments = returnDocuments;
    }

    public Boolean getTruncation() {
        return truncation;
    }

    public void setTruncation(Boolean truncation) {
        this.truncation = truncation;
    }

    public Boolean getLogRequests() {
        return logRequests;
    }

    public void setLogRequests(Boolean logRequests) {
        this.logRequests = logRequests;
    }

    public Boolean getLogResponses() {
        return logResponses;
    }

    public void setLogResponses(Boolean logResponses) {
        this.logResponses = logResponses;
    }
}
