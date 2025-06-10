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
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return AnthropicChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .version(chatModelProperties.getVersion())
                .beta(chatModelProperties.getBeta())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxTokens(chatModelProperties.getMaxTokens())
                .stopSequences(chatModelProperties.getStopSequences())
                .cacheSystemMessages(chatModelProperties.getCacheSystemMessages())
                .cacheTools(chatModelProperties.getCacheTools())
                .thinkingType(chatModelProperties.getThinkingType())
                .thinkingBudgetTokens(chatModelProperties.getThinkingBudgetTokens())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    AnthropicStreamingChatModel anthropicStreamingChatModel(Properties properties,
                                                            ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return AnthropicStreamingChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .version(chatModelProperties.getVersion())
                .beta(chatModelProperties.getBeta())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxTokens(chatModelProperties.getMaxTokens())
                .stopSequences(chatModelProperties.getStopSequences())
                .cacheSystemMessages(chatModelProperties.getCacheSystemMessages())
                .cacheTools(chatModelProperties.getCacheTools())
                .thinkingType(chatModelProperties.getThinkingType())
                .thinkingBudgetTokens(chatModelProperties.getThinkingBudgetTokens())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }
}