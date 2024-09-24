package dev.langchain4j.dashscope.spring;

import dev.langchain4j.model.dashscope.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".chat-model.api-key")
    QwenChatModel qwenChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return QwenChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .enableSearch(chatModelProperties.getEnableSearch())
                .seed(chatModelProperties.getSeed())
                .repetitionPenalty(chatModelProperties.getRepetitionPenalty())
                .temperature(chatModelProperties.getTemperature())
                .stops(chatModelProperties.getStops())
                .maxTokens(chatModelProperties.getMaxTokens())
                .build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-chat-model.api-key")
    QwenStreamingChatModel qwenStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return QwenStreamingChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .enableSearch(chatModelProperties.getEnableSearch())
                .seed(chatModelProperties.getSeed())
                .repetitionPenalty(chatModelProperties.getRepetitionPenalty())
                .temperature(chatModelProperties.getTemperature())
                .stops(chatModelProperties.getStops())
                .maxTokens(chatModelProperties.getMaxTokens())
                .build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".language-model.api-key")
    QwenLanguageModel qwenLanguageModel(Properties properties) {
        LanguageModelProperties languageModelProperties = properties.getLanguageModel();
        return QwenLanguageModel.builder()
                .baseUrl(languageModelProperties.getBaseUrl())
                .apiKey(languageModelProperties.getApiKey())
                .modelName(languageModelProperties.getModelName())
                .temperature(languageModelProperties.getTemperature())
                .topP(languageModelProperties.getTopP())
                .topK(languageModelProperties.getTopK())
                .enableSearch(languageModelProperties.getEnableSearch())
                .seed(languageModelProperties.getSeed())
                .repetitionPenalty(languageModelProperties.getRepetitionPenalty())
                .temperature(languageModelProperties.getTemperature())
                .stops(languageModelProperties.getStops())
                .maxTokens(languageModelProperties.getMaxTokens())
                .build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-language-model.api-key")
    QwenStreamingLanguageModel qwenStreamingLanguageModel(Properties properties) {
        LanguageModelProperties languageModelProperties = properties.getStreamingLanguageModel();
        return QwenStreamingLanguageModel.builder()
                .baseUrl(languageModelProperties.getBaseUrl())
                .apiKey(languageModelProperties.getApiKey())
                .modelName(languageModelProperties.getModelName())
                .temperature(languageModelProperties.getTemperature())
                .topP(languageModelProperties.getTopP())
                .topK(languageModelProperties.getTopK())
                .enableSearch(languageModelProperties.getEnableSearch())
                .seed(languageModelProperties.getSeed())
                .repetitionPenalty(languageModelProperties.getRepetitionPenalty())
                .temperature(languageModelProperties.getTemperature())
                .stops(languageModelProperties.getStops())
                .maxTokens(languageModelProperties.getMaxTokens())
                .build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".embedding-model.api-key")
    QwenEmbeddingModel qwenEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return QwenEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .apiKey(embeddingModelProperties.getApiKey())
                .modelName(embeddingModelProperties.getModelName())
                .build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".tokenizer.api-key")
    QwenTokenizer qwenTokenizer(Properties properties) {
        TokenizerProperties tokenizerProperties = properties.getTokenizer();
        return QwenTokenizer.builder()
                .apiKey(tokenizerProperties.getApiKey())
                .modelName(tokenizerProperties.getModelName())
                .build();
    }
}