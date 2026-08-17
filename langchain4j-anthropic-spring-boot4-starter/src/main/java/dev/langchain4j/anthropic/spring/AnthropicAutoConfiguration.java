package dev.langchain4j.anthropic.spring;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.anthropic.spring.AnthropicProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(AnthropicProperties.class)
public class AnthropicAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AnthropicAutoConfiguration.class);

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    AnthropicChatModel anthropicChatModel(AnthropicProperties properties, ObjectProvider<ChatModelListener> listeners) {
        AnthropicChatModelProperties chatModelProperties = properties.getChatModel();
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
                .toolChoice(chatModelProperties.getToolChoice())
                .cacheSystemMessages(chatModelProperties.getCacheSystemMessages())
                .cacheTools(chatModelProperties.getCacheTools())
                .thinkingType(chatModelProperties.getThinkingType())
                .thinkingBudgetTokens(chatModelProperties.getThinkingBudgetTokens())
                .returnThinking(chatModelProperties.getReturnThinking())
                .sendThinking(chatModelProperties.getSendThinking())
                .customParameters(chatModelProperties.getCustomParameters())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    AnthropicStreamingChatModel anthropicStreamingChatModel(AnthropicProperties properties,
                                                            ObjectProvider<ChatModelListener> listeners) {
        AnthropicChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        warnIfSet(chatModelProperties.getMaxRetries(), "streaming-chat-model", "max-retries",
                "a response that has already started streaming cannot be retried");

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
                .toolChoice(chatModelProperties.getToolChoice())
                .cacheSystemMessages(chatModelProperties.getCacheSystemMessages())
                .cacheTools(chatModelProperties.getCacheTools())
                .thinkingType(chatModelProperties.getThinkingType())
                .thinkingBudgetTokens(chatModelProperties.getThinkingBudgetTokens())
                .returnThinking(chatModelProperties.getReturnThinking())
                .sendThinking(chatModelProperties.getSendThinking())
                .customParameters(chatModelProperties.getCustomParameters())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    /**
     * Some properties are shared between the sync and the streaming variants of a model, but the streaming model
     * cannot honour all of them. Rather than silently ignoring such a property, say so once at startup.
     */
    private static void warnIfSet(Object value, String model, String property, String reason) {
        boolean set = value instanceof Collection<?> collection ? !collection.isEmpty() : value != null;
        if (set) {
            log.warn("{}.{}.{} is set, but it is ignored: {}", PREFIX, model, property, reason);
        }
    }

}