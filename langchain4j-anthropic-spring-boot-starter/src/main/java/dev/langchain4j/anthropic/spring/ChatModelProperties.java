package dev.langchain4j.anthropic.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
class ChatModelProperties {

    String baseUrl;
    String apiKey;
    String version;
    String modelName;
    Double temperature;
    Double topP;
    Integer topK;
    Integer maxTokens;
    List<String> stopSequences;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequests;
    Boolean logResponses;
}