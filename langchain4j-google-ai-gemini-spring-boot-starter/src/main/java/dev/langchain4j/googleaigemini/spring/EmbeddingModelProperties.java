package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel.TaskType;

import java.time.Duration;

public class EmbeddingModelProperties {

    private String titleMetadataKey;
    private String modelName;
    private Boolean logRequestsAndResponses;
    private Integer maxRetries;
    private Integer outputDimensionality;
    private TaskType taskType;
    private Duration timeout;

    public String getTitleMetadataKey() {
        return titleMetadataKey;
    }

    public void setTitleMetadataKey(String titleMetadataKey) {
        this.titleMetadataKey = titleMetadataKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Boolean isLogRequestsAndResponses() {
        return logRequestsAndResponses;
    }

    public void setLogRequestsAndResponses(boolean logRequestsAndResponses) {
        this.logRequestsAndResponses = logRequestsAndResponses;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getOutputDimensionality() {
        return outputDimensionality;
    }

    public void setOutputDimensionality(Integer outputDimensionality) {
        this.outputDimensionality = outputDimensionality;
    }

    public GoogleAiEmbeddingModel.TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(GoogleAiEmbeddingModel.TaskType taskType) {
        this.taskType = taskType;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
