package dev.langchain4j.mistralai.spring;

import java.time.Duration;
import java.util.List;

class FimModelProperties {

    String baseUrl;
    String apiKey;
    String modelName;
    Double temperature;
    Integer maxTokens;
    Integer minTokens;
    Double topP;
    Integer randomSeed;
    List<String> stop;
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

    public Double getTemperature() {
        return this.temperature;
    }

    public Integer getMaxTokens() {
        return this.maxTokens;
    }

    public Integer getMinTokens() {
        return this.minTokens;
    }

    public Double getTopP() {
        return this.topP;
    }

    public Integer getRandomSeed() {
        return this.randomSeed;
    }

    public List<String> getStop() {
        return this.stop;
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

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setMinTokens(Integer minTokens) {
        this.minTokens = minTokens;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public void setRandomSeed(Integer randomSeed) {
        this.randomSeed = randomSeed;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
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
