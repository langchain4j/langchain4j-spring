package dev.langchain4j.model.github.spring;

class EmbeddingModelProperties {

    private String endpoint;
    private String gitHubToken;
    private String modelName;
    private Integer dimensions;
    private Integer timeout;
    private Integer maxRetries;
    private Boolean logRequestsAndResponses;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getGitHubToken() {
        return gitHubToken;
    }

    public void setGitHubToken(String gitHubToken) {
        this.gitHubToken = gitHubToken;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Boolean getLogRequestsAndResponses() {
        return logRequestsAndResponses;
    }

    public void setLogRequestsAndResponses(Boolean logRequestsAndResponses) {
        this.logRequestsAndResponses = logRequestsAndResponses;
    }
}