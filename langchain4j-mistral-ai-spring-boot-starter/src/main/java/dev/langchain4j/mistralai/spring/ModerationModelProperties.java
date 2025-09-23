package dev.langchain4j.mistralai.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
class ModerationModelProperties {

    String baseUrl;
    String apiKey;
    String modelName;
    Duration timeout;
    Boolean logRequests;
    Boolean logResponses;
    Integer maxRetries;
}
