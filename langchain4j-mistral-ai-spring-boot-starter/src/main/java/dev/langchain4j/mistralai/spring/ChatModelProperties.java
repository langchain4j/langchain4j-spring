package dev.langchain4j.mistralai.spring;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.request.ResponseFormat;

import java.time.Duration;
import java.util.List;
import java.util.Set;

class ChatModelProperties {

    String baseUrl;
    String apiKey;
    String modelName;
    Double temperature;
    Double topP;
    Integer maxTokens;
    Boolean safePrompt;
    Integer randomSeed;
    ResponseFormat responseFormat;
    List<String> stopSequences;
    Double frequencyPenalty;
    Double presencePenalty;
    Duration timeout;
    Boolean logRequests;
    Boolean logResponses;
    Integer maxRetries;
    Set<Capability> supportedCapabilities;

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

    public Double getTopP() {
        return this.topP;
    }

    public Integer getMaxTokens() {
        return this.maxTokens;
    }

    public Boolean getSafePrompt() {
        return this.safePrompt;
    }

    public Integer getRandomSeed() {
        return this.randomSeed;
    }

    public ResponseFormat getResponseFormat() {
        return this.responseFormat;
    }

    public List<String> getStopSequences() {
        return this.stopSequences;
    }

    public Double getFrequencyPenalty() {
        return this.frequencyPenalty;
    }

    public Double getPresencePenalty() {
        return this.presencePenalty;
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

    public Set<Capability> getSupportedCapabilities() {
        return this.supportedCapabilities;
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

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setSafePrompt(Boolean safePrompt) {
        this.safePrompt = safePrompt;
    }

    public void setRandomSeed(Integer randomSeed) {
        this.randomSeed = randomSeed;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    public void setStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
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

    public void setSupportedCapabilities(Set<Capability> supportedCapabilities) {
        this.supportedCapabilities = supportedCapabilities;
    }
}
