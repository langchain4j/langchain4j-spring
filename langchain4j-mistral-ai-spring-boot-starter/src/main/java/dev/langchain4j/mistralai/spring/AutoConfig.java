package dev.langchain4j.mistralai.spring;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiFimModel;
import dev.langchain4j.model.mistralai.MistralAiModerationModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingFimModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.mistralai.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    MistralAiChatModel mistralAiChatModel(Properties properties, ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return MistralAiChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .safePrompt(chatModelProperties.getSafePrompt())
                .randomSeed(chatModelProperties.getRandomSeed())
                .responseFormat(chatModelProperties.getResponseFormat())
                .stopSequences(chatModelProperties.getStopSequences())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .maxRetries(chatModelProperties.getMaxRetries())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    MistralAiStreamingChatModel mistralAiStreamingChatModel(Properties properties,
                                                            ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return MistralAiStreamingChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .safePrompt(chatModelProperties.getSafePrompt())
                .randomSeed(chatModelProperties.getRandomSeed())
                .responseFormat(chatModelProperties.getResponseFormat())
                .stopSequences(chatModelProperties.getStopSequences())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    MistralAiEmbeddingModel mistralAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return MistralAiEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .apiKey(embeddingModelProperties.getApiKey())
                .modelName(embeddingModelProperties.getModelName())
                .timeout(embeddingModelProperties.getTimeout())
                .logRequests(embeddingModelProperties.getLogRequests())
                .logResponses(embeddingModelProperties.getLogResponses())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".fim-model.api-key")
    MistralAiFimModel mistralAiFimModel(Properties properties) {
        FimModelProperties fimModelProperties = properties.getFimModel();
        return MistralAiFimModel.builder()
                .baseUrl(fimModelProperties.getBaseUrl())
                .apiKey(fimModelProperties.getApiKey())
                .modelName(fimModelProperties.getModelName())
                .temperature(fimModelProperties.getTemperature())
                .maxTokens(fimModelProperties.getMaxTokens())
                .minTokens(fimModelProperties.getMinTokens())
                .topP(fimModelProperties.getTopP())
                .randomSeed(fimModelProperties.getRandomSeed())
                .stop(fimModelProperties.getStop())
                .timeout(fimModelProperties.getTimeout())
                .logRequests(fimModelProperties.getLogRequests())
                .logResponses(fimModelProperties.getLogResponses())
                .maxRetries(fimModelProperties.getMaxRetries())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-fim-model.api-key")
    MistralAiStreamingFimModel mistralAiStreamingFimModel(Properties properties) {
        FimModelProperties fimModelProperties = properties.getStreamingFimModel();
        return MistralAiStreamingFimModel.builder()
                .baseUrl(fimModelProperties.getBaseUrl())
                .apiKey(fimModelProperties.getApiKey())
                .modelName(fimModelProperties.getModelName())
                .temperature(fimModelProperties.getTemperature())
                .maxTokens(fimModelProperties.getMaxTokens())
                .minTokens(fimModelProperties.getMinTokens())
                .topP(fimModelProperties.getTopP())
                .randomSeed(fimModelProperties.getRandomSeed())
                .stop(fimModelProperties.getStop())
                .timeout(fimModelProperties.getTimeout())
                .logRequests(fimModelProperties.getLogRequests())
                .logResponses(fimModelProperties.getLogResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    MistralAiModerationModel mistralAiModerationModel(Properties properties) {
        ModerationModelProperties moderationModelProperties = properties.getModerationModel();
        return new MistralAiModerationModel.Builder()
                .baseUrl(moderationModelProperties.getBaseUrl())
                .apiKey(moderationModelProperties.getApiKey())
                .modelName(moderationModelProperties.getModelName())
                .timeout(moderationModelProperties.getTimeout())
                .logRequests(moderationModelProperties.getLogRequests())
                .logResponses(moderationModelProperties.getLogResponses())
                .maxRetries(moderationModelProperties.getMaxRetries())
                .build();
    }
}
