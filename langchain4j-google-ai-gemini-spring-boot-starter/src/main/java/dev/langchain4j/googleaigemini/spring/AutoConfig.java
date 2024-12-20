package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;

import static dev.langchain4j.googleaigemini.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty({
            PREFIX + ".chat-model.api-key",
            PREFIX + ".chat-model.model-name"
    })
    GoogleAiGeminiChatModel googleAiGeminiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GoogleAiGeminiChatModel.builder()
                .apiKey(chatModelProperties.apiKey())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .topK(chatModelProperties.topK())
                .maxOutputTokens(chatModelProperties.maxOutputTokens())
                .responseFormat(chatModelProperties.responseFormat())
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses())
                .safetySettings(Map.of(
                        // TODO NPE?
                        chatModelProperties.safetySetting().geminiHarmCategory(),
                        chatModelProperties.safetySetting().geminiHarmBlockThreshold()
                ))
                .toolConfig(
                        // TODO NPE?
                        chatModelProperties.functionCallingConfig().geminiMode(),
                        chatModelProperties.functionCallingConfig().allowedFunctionNames().toArray(new String[0])
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty({
            PREFIX + ".streaming-chat-model.api-key",
            PREFIX + ".streaming-chat-model.model-name"
    })
    GoogleAiGeminiStreamingChatModel googleAiGeminiStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(chatModelProperties.apiKey())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .topK(chatModelProperties.topK())
                .responseFormat(chatModelProperties.responseFormat())
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses())
                .safetySettings(Map.of(
                        // TODO NPE?
                        chatModelProperties.safetySetting().geminiHarmCategory(),
                        chatModelProperties.safetySetting().geminiHarmBlockThreshold()
                ))
                .toolConfig(
                        // TODO NPE?
                        chatModelProperties.functionCallingConfig().geminiMode(),
                        chatModelProperties.functionCallingConfig().allowedFunctionNames().toArray(new String[0])
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty({
            PREFIX + ".embedding-model.api-key",
            PREFIX + ".embedding-model.model-name"
    })
    GoogleAiEmbeddingModel googleAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return GoogleAiEmbeddingModel.builder()
                .apiKey(embeddingModelProperties.apiKey())
                .modelName(embeddingModelProperties.modelName())
                .logRequestsAndResponses(embeddingModelProperties.logRequestsAndResponses())
                .maxRetries(embeddingModelProperties.maxRetries())
                .outputDimensionality(embeddingModelProperties.outputDimensionality())
                .taskType(embeddingModelProperties.taskType())
                .timeout(embeddingModelProperties.timeout())
                .titleMetadataKey(embeddingModelProperties.titleMetadataKey())
                .build();
    }
}