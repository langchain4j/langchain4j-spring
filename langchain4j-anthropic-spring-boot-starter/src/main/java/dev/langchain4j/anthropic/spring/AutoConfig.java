package dev.langchain4j.anthropic.spring;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.anthropic.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    AnthropicChatModel anthropicChatModel(Properties properties, ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        return AnthropicChatModel.builder()
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .version(chatModelProperties.version())
                .beta(chatModelProperties.beta())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .topK(chatModelProperties.topK())
                .maxTokens(chatModelProperties.maxTokens())
                .stopSequences(chatModelProperties.stopSequences())
                .cacheSystemMessages(chatModelProperties.cacheSystemMessages())
                .cacheTools(chatModelProperties.cacheTools())
                .thinkingType(chatModelProperties.thinkingType())
                .thinkingBudgetTokens(chatModelProperties.thinkingBudgetTokens())
                .timeout(chatModelProperties.timeout())
                .maxRetries(chatModelProperties.maxRetries())
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    AnthropicStreamingChatModel anthropicStreamingChatModel(Properties properties,
                                                            ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.streamingChatModel();
        return AnthropicStreamingChatModel.builder()
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .version(chatModelProperties.version())
                .beta(chatModelProperties.beta())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .topK(chatModelProperties.topK())
                .maxTokens(chatModelProperties.maxTokens())
                .stopSequences(chatModelProperties.stopSequences())
                .cacheSystemMessages(chatModelProperties.cacheSystemMessages())
                .cacheTools(chatModelProperties.cacheTools())
                .thinkingType(chatModelProperties.thinkingType())
                .thinkingBudgetTokens(chatModelProperties.thinkingBudgetTokens())
                .timeout(chatModelProperties.timeout())
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }
}