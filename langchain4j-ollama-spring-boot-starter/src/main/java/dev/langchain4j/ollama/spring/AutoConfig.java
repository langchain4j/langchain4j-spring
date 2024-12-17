package dev.langchain4j.ollama.spring;

import dev.langchain4j.model.ollama.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.ollama.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.base-url")
    OllamaChatModel ollamaChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return OllamaChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .stop(chatModelProperties.getStop())
                .format(chatModelProperties.getFormat())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    OllamaStreamingChatModel ollamaStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return OllamaStreamingChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .stop(chatModelProperties.getStop())
                .format(chatModelProperties.getFormat())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .timeout(chatModelProperties.getTimeout())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.base-url")
    OllamaLanguageModel ollamaLanguageModel(Properties properties) {
        LanguageModelProperties languageModelProperties = properties.getLanguageModel();
        return OllamaLanguageModel.builder()
                .baseUrl(languageModelProperties.getBaseUrl())
                .modelName(languageModelProperties.getModelName())
                .temperature(languageModelProperties.getTemperature())
                .topK(languageModelProperties.getTopK())
                .topP(languageModelProperties.getTopP())
                .repeatPenalty(languageModelProperties.getRepeatPenalty())
                .seed(languageModelProperties.getSeed())
                .numPredict(languageModelProperties.getNumPredict())
                .stop(languageModelProperties.getStop())
                .format(languageModelProperties.getFormat())
                .timeout(languageModelProperties.getTimeout())
                .maxRetries(languageModelProperties.getMaxRetries())
                .customHeaders(languageModelProperties.getCustomHeaders())
                .logRequests(languageModelProperties.getLogRequests())
                .logResponses(languageModelProperties.getLogResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    OllamaStreamingLanguageModel ollamaStreamingLanguageModel(Properties properties) {
        LanguageModelProperties languageModelProperties = properties.getStreamingLanguageModel();
        return OllamaStreamingLanguageModel.builder()
                .baseUrl(languageModelProperties.getBaseUrl())
                .modelName(languageModelProperties.getModelName())
                .temperature(languageModelProperties.getTemperature())
                .topK(languageModelProperties.getTopK())
                .topP(languageModelProperties.getTopP())
                .repeatPenalty(languageModelProperties.getRepeatPenalty())
                .seed(languageModelProperties.getSeed())
                .numPredict(languageModelProperties.getNumPredict())
                .stop(languageModelProperties.getStop())
                .format(languageModelProperties.getFormat())
                .timeout(languageModelProperties.getTimeout())
                .customHeaders(languageModelProperties.getCustomHeaders())
                .logRequests(languageModelProperties.getLogRequests())
                .logResponses(languageModelProperties.getLogResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.base-url")
    OllamaEmbeddingModel ollamaEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return OllamaEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .modelName(embeddingModelProperties.getModelName())
                .timeout(embeddingModelProperties.getTimeout())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .customHeaders(embeddingModelProperties.getCustomHeaders())
                .logRequests(embeddingModelProperties.getLogRequests())
                .logResponses(embeddingModelProperties.getLogResponses())
                .build();
    }
}