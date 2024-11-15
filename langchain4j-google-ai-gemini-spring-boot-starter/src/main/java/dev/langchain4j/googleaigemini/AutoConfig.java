package dev.langchain4j.googleaigemini;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.googleaigemini.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".chatModel.enabled", havingValue = "true")
    ChatLanguageModel googleAiGeminiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GoogleAiGeminiChatModel.builder()
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxOutputTokens(chatModelProperties.getMaxOutputTokens())
                .responseFormat(chatModelProperties.getResponseFormat())
                .logRequestsAndResponses(chatModelProperties.isLogRequestsAndResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".streamingChatModel.enabled", havingValue = "true")
    StreamingChatLanguageModel googleAiGeminiStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .responseFormat(chatModelProperties.getResponseFormat())
                .responseFormat(chatModelProperties.getResponseFormat())
                .logRequestsAndResponses(chatModelProperties.isLogRequestsAndResponses())
                .build();
    }
}