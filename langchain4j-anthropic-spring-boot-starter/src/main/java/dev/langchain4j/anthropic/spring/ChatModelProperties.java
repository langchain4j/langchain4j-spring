package dev.langchain4j.anthropic.spring;

import dev.langchain4j.model.chat.request.ToolChoice;
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
    String beta;
    String modelName;
    Double temperature;
    Double topP;
    Integer topK;
    Integer maxTokens;
    List<String> stopSequences;
    ToolChoice toolChoice;
    Boolean cacheSystemMessages;
    Boolean cacheTools;
    String thinkingType;
    Integer thinkingBudgetTokens;
    Boolean returnThinking;
    Boolean sendThinking;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequests;
    Boolean logResponses;
}