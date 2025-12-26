package dev.langchain4j.azure.openai.spring;

import dev.langchain4j.model.chat.Capability;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Properties for configuring Azure OpenAI Chat Model.
 */
class ChatModelProperties {

    private String endpoint;
    private String serviceVersion;
    private String apiKey;
    private String deploymentName;
    private Integer maxTokens;
    private Integer maxCompletionTokens;
    private Double temperature;
    private Double topP;
    private Map<String, Integer> logitBias;
    private String user;
    private List<String> stop;
    private Double presencePenalty;
    private Double frequencyPenalty;
    private Long seed;
    private Boolean strictJsonSchema;
    
    // New Duration-based timeout (recommended)
    private Duration timeout;
    
    // Legacy Integer-based timeout (deprecated, for backward compatibility)
    @Deprecated
    private Integer timeoutSeconds;
    
    private Integer maxRetries;
    private Boolean logRequestsAndResponses;
    private String userAgentSuffix;
    private Map<String, String> customHeaders;
    private String nonAzureApiKey;
    private Set<Capability> supportedCapabilities;

    // Getters and Setters

    public String endpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String serviceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String apiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String deploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
    }

    public void setMaxCompletionTokens(Integer maxCompletionTokens) {
        this.maxCompletionTokens = maxCompletionTokens;
    }

    public Double temperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double topP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Map<String, Integer> logitBias() {
        return logitBias;
    }

    public void setLogitBias(Map<String, Integer> logitBias) {
        this.logitBias = logitBias;
    }

    public String user() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> stop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public Double presencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public Double frequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Long seed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public Boolean strictJsonSchema() {
        return strictJsonSchema;
    }

    public void setStrictJsonSchema(Boolean strictJsonSchema) {
        this.strictJsonSchema = strictJsonSchema;
    }

    /**
     * Returns the timeout configuration.
     * <p>
     * This method supports backward compatibility:
     * 1. If the new Duration-based {@code timeout} is set, it will be returned.
     * 2. If only the legacy Integer-based {@code timeoutSeconds} is set, it will be converted to Duration.
     * 3. If neither is set, returns null.
     * </p>
     * <p>
     * <b>Migration Guide:</b><br>
     * Old (deprecated): {@code timeout-seconds: 60}<br>
     * New (recommended): {@code timeout: 60s}
     * </p>
     *
     * @return the timeout as a Duration, or null if not configured
     */
    public Duration timeout() {
        // Prefer new Duration-based timeout
        if (timeout != null) {
            return timeout;
        }
        // Fall back to legacy Integer-based timeout for backward compatibility
        if (timeoutSeconds != null) {
            return Duration.ofSeconds(timeoutSeconds);
        }
        return null;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * @deprecated Use {@link #setTimeout(Duration)} instead.
     * For example: {@code timeout: 60s} instead of {@code timeout-seconds: 60}
     */
    @Deprecated
    public Integer timeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * @deprecated Use {@link #setTimeout(Duration)} instead.
     * For example: {@code timeout: 60s} instead of {@code timeout-seconds: 60}
     */
    @Deprecated
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer maxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Boolean logRequestsAndResponses() {
        return logRequestsAndResponses;
    }

    public void setLogRequestsAndResponses(Boolean logRequestsAndResponses) {
        this.logRequestsAndResponses = logRequestsAndResponses;
    }

    public String userAgentSuffix() {
        return userAgentSuffix;
    }

    public void setUserAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
    }

    public Map<String, String> customHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public String nonAzureApiKey() {
        return nonAzureApiKey;
    }

    public void setNonAzureApiKey(String nonAzureApiKey) {
        this.nonAzureApiKey = nonAzureApiKey;
    }

    public Set<Capability> supportedCapabilities() {
        return supportedCapabilities;
    }

    public void setSupportedCapabilities(Set<Capability> supportedCapabilities) {
        this.supportedCapabilities = supportedCapabilities;
    }
}