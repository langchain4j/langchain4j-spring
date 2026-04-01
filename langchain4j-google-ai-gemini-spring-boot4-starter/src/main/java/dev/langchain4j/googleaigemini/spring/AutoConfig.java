package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.googleai.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.stream.Collectors;

import static dev.langchain4j.googleaigemini.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
class AutoConfig {

    @Bean
    @ConditionalOnProperty({
            PREFIX + ".chat-model.api-key",
            PREFIX + ".chat-model.model-name"
    })
    GoogleAiGeminiChatModel googleAiGeminiChatModel(Properties properties,
                                                    ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder = GoogleAiGeminiChatModel.builder()
                .apiKey(chatModelProperties.apiKey())
                .baseUrl(chatModelProperties.baseUrl())
                .modelName(chatModelProperties.modelName())
                .maxRetries(chatModelProperties.maxRetries())
                .temperature(chatModelProperties.temperature())
                .topK(chatModelProperties.topK())
                .seed(chatModelProperties.seed())
                .topP(chatModelProperties.topP())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .presencePenalty(chatModelProperties.presencePenalty())
                .maxOutputTokens(chatModelProperties.maxOutputTokens())
                .timeout(chatModelProperties.timeout())
                .stopSequences(chatModelProperties.stopSequences())
                .allowCodeExecution(chatModelProperties.allowCodeExecution())
                .includeCodeExecutionOutput(chatModelProperties.includeCodeExecutionOutput())
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses())
                .listeners(listeners.orderedStream().toList());

        if (chatModelProperties.safetySetting() != null && !chatModelProperties.safetySetting().isEmpty()) {
            builder.safetySettings(convertSafetySettings(chatModelProperties.safetySetting()));
        }

        if (chatModelProperties.functionCallingConfig() != null) {
            builder.toolConfig(chatModelProperties
                            .functionCallingConfig()
                            .geminiMode(),
                    chatModelProperties
                            .functionCallingConfig()
                            .allowedFunctionNames()
                            .toArray(new String[0]));
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty({
            PREFIX + ".streaming-chat-model.api-key",
            PREFIX + ".streaming-chat-model.model-name"
    })
    GoogleAiGeminiStreamingChatModel googleAiGeminiStreamingChatModel(Properties properties,
                                                                      ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder = GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(chatModelProperties.apiKey())
                .baseUrl(chatModelProperties.baseUrl())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topK(chatModelProperties.topK())
                .seed(chatModelProperties.seed())
                .topP(chatModelProperties.topP())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .presencePenalty(chatModelProperties.presencePenalty())
                .maxOutputTokens(chatModelProperties.maxOutputTokens())
                .timeout(chatModelProperties.timeout())
                .stopSequences(chatModelProperties.stopSequences())
                .allowCodeExecution(chatModelProperties.allowCodeExecution())
                .includeCodeExecutionOutput(chatModelProperties.includeCodeExecutionOutput())
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses())
                .listeners(listeners.orderedStream().toList());


        if (chatModelProperties.safetySetting() != null && !chatModelProperties.safetySetting().isEmpty()) {
            builder.safetySettings(convertSafetySettings(chatModelProperties.safetySetting()));
        }

        if (chatModelProperties
                .functionCallingConfig() != null) {
            builder.toolConfig(chatModelProperties
                            .functionCallingConfig()
                            .geminiMode(),
                    chatModelProperties
                            .functionCallingConfig()
                            .allowedFunctionNames()
                            .toArray(new String[0]));
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty({
            PREFIX + ".embedding-model.api-key",
            PREFIX + ".embedding-model.model-name"
    })
    GoogleAiEmbeddingModel googleAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return GoogleAiEmbeddingModel.builder()
                .modelName(embeddingModelProperties.modelName())
                .apiKey(embeddingModelProperties.apiKey())
                .maxRetries(embeddingModelProperties.maxRetries())
                .taskType(embeddingModelProperties.taskType())
                .titleMetadataKey(embeddingModelProperties.titleMetadataKey())
                .outputDimensionality(embeddingModelProperties.outputDimensionality())
                .timeout(embeddingModelProperties.timeout())
                .logRequestsAndResponses(embeddingModelProperties.logRequestsAndResponses())
                .build();
    }

    private Map<GeminiHarmCategory, GeminiHarmBlockThreshold> convertSafetySettings(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> GeminiHarmCategory.valueOf(e.getKey()),
                        e -> GeminiHarmBlockThreshold.valueOf(e.getValue())
                ));
    }
}