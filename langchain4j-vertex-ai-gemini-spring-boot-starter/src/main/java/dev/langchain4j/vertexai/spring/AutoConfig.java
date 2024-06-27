package dev.langchain4j.vertexai.spring;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiStreamingChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.vertexai.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".chat-model.enabled", havingValue = "true")
    VertexAiGeminiChatModel vertexAiGeminiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return VertexAiGeminiChatModel.builder()
                .project(chatModelProperties.getProject())
                .location(chatModelProperties.getLocation())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .maxOutputTokens(chatModelProperties.getMaxOutputTokens())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .maxRetries(chatModelProperties.getMaxRetries())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".streaming-chat-model.enabled", havingValue = "true")
    VertexAiGeminiStreamingChatModel vertexAiGeminiStreamingChatModel(Properties properties) {
        ChatModelProperties streamingChatProperties = properties.getStreamingChatModel();
        return VertexAiGeminiStreamingChatModel.builder()
                .project(streamingChatProperties.getProject())
                .location(streamingChatProperties.getLocation())
                .modelName(streamingChatProperties.getModelName())
                .temperature(streamingChatProperties.getTemperature())
                .maxOutputTokens(streamingChatProperties.getMaxOutputTokens())
                .topK(streamingChatProperties.getTopK())
                .topP(streamingChatProperties.getTopP())
                .build();
    }

}
