package dev.langchain4j.azure.openai.spring;

import java.time.Duration;
import java.util.Map;

/**
 * Properties for configuring Azure OpenAI Image Model.
 */
class ImageModelProperties {

    private String endpoint;
    private String serviceVersion;
    private String apiKey;
    private String deploymentName;
    private String quality;
    private String size;
    private String user;
    private String style;
    private String responseFormat;
    
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

    public String quality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String size() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String user() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String style() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String responseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    /**
     * Returns the timeout configuration with backward compatibility support.
     * <p>
     * Prefers new Duration-based {@code timeout}, falls back to legacy {@code timeoutSeconds}.
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
        if (timeout != null) {
            return timeout;
        }
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
}