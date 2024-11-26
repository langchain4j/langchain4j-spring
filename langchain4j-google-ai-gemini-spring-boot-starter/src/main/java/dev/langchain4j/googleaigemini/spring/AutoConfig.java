package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
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
    @ConditionalOnProperty(name = PREFIX + ".chatModel.enabled", havingValue = "true")
    ChatLanguageModel googleAiGeminiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GoogleAiGeminiChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxOutputTokens(chatModelProperties.getMaxOutputTokens())
                .responseFormat(chatModelProperties.getResponseFormat())
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses())
                .safetySettings(
                        Map.of(chatModelProperties.getSafetySetting().getGeminiHarmCategory(),
                                chatModelProperties.getSafetySetting().getGeminiHarmBlockThreshold()))
                .toolConfig(
                        chatModelProperties.getFunctionCallingConfig().getGeminiMode(),
                        chatModelProperties.getFunctionCallingConfig().getAllowedFunctionNames().toArray(new String[0]))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".streamingChatModel.enabled", havingValue = "true")
    StreamingChatLanguageModel googleAiGeminiStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .responseFormat(chatModelProperties.getResponseFormat())
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".embeddingModel.enabled", havingValue = "true")
    EmbeddingModel googleAiGeminiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return GoogleAiEmbeddingModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(embeddingModelProperties.getModelName())
                .logRequestsAndResponses(embeddingModelProperties.isLogRequestsAndResponses())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .outputDimensionality(embeddingModelProperties.getOutputDimensionality())
                .taskType(embeddingModelProperties.getTaskType())
                .timeout(embeddingModelProperties.getTimeout())
                .titleMetadataKey(embeddingModelProperties.getTitleMetadataKey())
                .build();
    }
}