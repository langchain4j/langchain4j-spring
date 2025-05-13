package dev.langchain4j.vertexai.spring;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiStreamingChatModel;
import org.springframework.beans.factory.ObjectProvider;
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
    VertexAiGeminiChatModel vertexAiGeminiChatModel(Properties properties,
                                                    ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        return VertexAiGeminiChatModel.builder()
                .project(chatModelProperties.project())
                .location(chatModelProperties.location())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .maxOutputTokens(chatModelProperties.maxOutputTokens())
                .topK(chatModelProperties.topK())
                .topP(chatModelProperties.topP())
                .maxRetries(chatModelProperties.maxRetries())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".streaming-chat-model.enabled", havingValue = "true")
    VertexAiGeminiStreamingChatModel vertexAiGeminiStreamingChatModel(Properties properties,
                                                                      ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties streamingChatProperties = properties.streamingChatModel();
        return VertexAiGeminiStreamingChatModel.builder()
                .project(streamingChatProperties.project())
                .location(streamingChatProperties.location())
                .modelName(streamingChatProperties.modelName())
                .temperature(streamingChatProperties.temperature())
                .maxOutputTokens(streamingChatProperties.maxOutputTokens())
                .topK(streamingChatProperties.topK())
                .topP(streamingChatProperties.topP())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

}
