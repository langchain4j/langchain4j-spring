package dev.langchain4j.ollama.spring;

import dev.langchain4j.model.chat.Capability;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatModelProperties {

    String baseUrl;
    String modelName;
    Double temperature;
    Integer topK;
    Double topP;
    Integer mirostat;
    Double mirostatEta;
    Double mirostatTau;
    Integer repeatLastN;
    Double repeatPenalty;
    Integer seed;
    Integer numPredict;
    Integer numCtx;
    List<String> stop;
    Double minP;
    Set<Capability> supportedCapabilities;
    Boolean think;
    Boolean returnThinking;
    Duration timeout;
    Integer maxRetries;
    Map<String, String> customHeaders;
    Boolean logRequests;
    Boolean logResponses;

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMirostat() {
        return mirostat;
    }

    public void setMirostat(Integer mirostat) {
        this.mirostat = mirostat;
    }

    public Double getMirostatEta() {
        return mirostatEta;
    }

    public void setMirostatEta(Double mirostatEta) {
        this.mirostatEta = mirostatEta;
    }

    public Double getMirostatTau() {
        return mirostatTau;
    }

    public void setMirostatTau(Double mirostatTau) {
        this.mirostatTau = mirostatTau;
    }

    public Integer getRepeatLastN() {
        return repeatLastN;
    }

    public void setRepeatLastN(Integer repeatLastN) {
        this.repeatLastN = repeatLastN;
    }

    public Double getRepeatPenalty() {
        return repeatPenalty;
    }

    public void setRepeatPenalty(Double repeatPenalty) {
        this.repeatPenalty = repeatPenalty;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getNumPredict() {
        return numPredict;
    }

    public void setNumPredict(Integer numPredict) {
        this.numPredict = numPredict;
    }

    public Integer getNumCtx() {
        return numCtx;
    }

    public void setNumCtx(Integer numCtx) {
        this.numCtx = numCtx;
    }

    public Double getMinP() {
        return minP;
    }

    public void setMinP(Double minP) {
        this.minP = minP;
    }

    public Set<Capability> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    public void setSupportedCapabilities(Set<Capability> supportedCapabilities) {
        this.supportedCapabilities = supportedCapabilities;
    }

    public Boolean getThink() {
        return think;
    }

    public void setThink(Boolean think) {
        this.think = think;
    }

    public Boolean getReturnThinking() {
        return returnThinking;
    }

    public void setReturnThinking(Boolean returnThinking) {
        this.returnThinking = returnThinking;
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

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
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