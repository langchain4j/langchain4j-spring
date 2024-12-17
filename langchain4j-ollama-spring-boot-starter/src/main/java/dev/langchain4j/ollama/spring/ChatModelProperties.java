package dev.langchain4j.ollama.spring;

import dev.langchain4j.model.chat.Capability;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
class ChatModelProperties {

    String baseUrl;
    String modelName;
    Double temperature;
    Integer topK;
    Double topP;
    Double repeatPenalty;
    Integer seed;
    Integer numPredict;
    List<String> stop;
    String format;
    Set<Capability> supportedCapabilities;
    Duration timeout;
    Integer maxRetries;
    Map<String, String> customHeaders;
    Boolean logRequests;
    Boolean logResponses;
}