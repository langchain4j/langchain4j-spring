package dev.langchain4j.voyageai.spring;

import java.time.Duration;

public class EmbeddingModelProperties {

    private String baseUrl;
    private Duration timeout;
    private Integer maxRetries;
    private String apiKey;
    private String modelName;
    private String inputType;
    private Boolean truncation;
    private String encodingFormat;
    private Boolean logRequests;
    private Boolean logResponses;
    private Integer maxSegmentsPerBatch;

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

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public Boolean getTruncation() {
        return truncation;
    }

    public void setTruncation(Boolean truncation) {
        this.truncation = truncation;
    }

    public String getEncodingFormat() {
        return encodingFormat;
    }

    public void setEncodingFormat(String encodingFormat) {
        this.encodingFormat = encodingFormat;
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

    public Integer getMaxSegmentsPerBatch() {
        return maxSegmentsPerBatch;
    }

    public void setMaxSegmentsPerBatch(Integer maxSegmentsPerBatch) {
        this.maxSegmentsPerBatch = maxSegmentsPerBatch;
    }
}
