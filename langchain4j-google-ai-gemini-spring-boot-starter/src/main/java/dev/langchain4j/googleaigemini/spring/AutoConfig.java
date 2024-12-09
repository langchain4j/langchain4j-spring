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
    @ConditionalOnProperty(name = {PREFIX + ".chatModel.api-key", PREFIX + ".chatModel.model-name"})
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
                .safetySettings(
                        Map.of(chatModelProperties.safetySetting().geminiHarmCategory(),
                                chatModelProperties.safetySetting().geminiHarmBlockThreshold()))
                .toolConfig(
                        chatModelProperties.functionCallingConfig().getGeminiMode(),
                        chatModelProperties.functionCallingConfig().getAllowedFunctionNames().toArray(new String[0]))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = {PREFIX + ".streamingChatModel.api-key", PREFIX + ".streamingChatModel.model-name"})
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
                .safetySettings(
                        Map.of(chatModelProperties.safetySetting().geminiHarmCategory(),
                                chatModelProperties.safetySetting().geminiHarmBlockThreshold()))
                .toolConfig(
                        chatModelProperties.functionCallingConfig().getGeminiMode(),
                        chatModelProperties.functionCallingConfig().getAllowedFunctionNames().toArray(new String[0]))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".embeddingModel.enabled", havingValue = "true")
    GoogleAiEmbeddingModel googleAiGeminiEmbeddingModel(Properties properties) {
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