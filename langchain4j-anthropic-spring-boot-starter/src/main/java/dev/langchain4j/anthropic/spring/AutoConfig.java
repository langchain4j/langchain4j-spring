package dev.langchain4j.anthropic.spring;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
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
    AnthropicChatModel anthropicChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return AnthropicChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .version(chatModelProperties.getVersion())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxTokens(chatModelProperties.getMaxTokens())
                .stopSequences(chatModelProperties.getStopSequences())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    AnthropicStreamingChatModel anthropicStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return AnthropicStreamingChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .version(chatModelProperties.getVersion())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxTokens(chatModelProperties.getMaxTokens())
                .stopSequences(chatModelProperties.getStopSequences())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .build();
    }
}