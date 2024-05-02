package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class ImageModelProperties {

    String endpoint;
    String apiKey;
    String nonAzureApiKey;
    String deploymentName;
    String size;
    String quality;
    String style;
    String responseFormat;
    String user;
    Integer timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
}