package dev.langchain4j.anthropic.spring;

import dev.langchain4j.model.chat.request.ToolChoice;

import java.time.Duration;
import java.util.List;
import java.util.Map;

class ChatModelProperties {

    String baseUrl;
    String apiKey;
    String version;
    String beta;
    String modelName;
    Double temperature;
    Double topP;
    Integer topK;
    Integer maxTokens;
    List<String> stopSequences;
    ToolChoice toolChoice;
    Boolean cacheSystemMessages;
    Boolean cacheTools;
    String thinkingType;
    Integer thinkingBudgetTokens;
    Boolean returnThinking;
    Boolean sendThinking;
    Map<String, Object> customParameters;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequests;
    Boolean logResponses;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBeta() {
        return beta;
    }

    public void setBeta(String beta) {
        this.beta = beta;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<String> getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
    }

    public ToolChoice getToolChoice() {
        return toolChoice;
    }

    public void setToolChoice(ToolChoice toolChoice) {
        this.toolChoice = toolChoice;
    }

    public Boolean getCacheSystemMessages() {
        return cacheSystemMessages;
    }

    public void setCacheSystemMessages(Boolean cacheSystemMessages) {
        this.cacheSystemMessages = cacheSystemMessages;
    }

    public Boolean getCacheTools() {
        return cacheTools;
    }

    public void setCacheTools(Boolean cacheTools) {
        this.cacheTools = cacheTools;
    }

    public String getThinkingType() {
        return thinkingType;
    }

    public void setThinkingType(String thinkingType) {
        this.thinkingType = thinkingType;
    }

    public Integer getThinkingBudgetTokens() {
        return thinkingBudgetTokens;
    }

    public void setThinkingBudgetTokens(Integer thinkingBudgetTokens) {
        this.thinkingBudgetTokens = thinkingBudgetTokens;
    }

    public Boolean getReturnThinking() {
        return returnThinking;
    }

    public void setReturnThinking(Boolean returnThinking) {
        this.returnThinking = returnThinking;
    }

    public Boolean getSendThinking() {
        return sendThinking;
    }

    public void setSendThinking(Boolean sendThinking) {
        this.sendThinking = sendThinking;
    }

    public Map<String, Object> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(Map<String, Object> customParameters) {
        this.customParameters = customParameters;
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