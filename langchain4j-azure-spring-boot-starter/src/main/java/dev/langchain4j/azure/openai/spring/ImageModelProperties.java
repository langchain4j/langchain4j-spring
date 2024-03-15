package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.time.Duration;

@Getter
@Setter
class ImageModelProperties {

    String endpoint;
    String apiKey;
    String organizationId;
    String deploymentName;
    String size;
    String quality;
    String style;
    String user;
    String responseFormat;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
    Boolean withPersisting;
    Path persistTo;
}