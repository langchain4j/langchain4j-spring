package dev.langchain4j.openai.spring;

import dev.langchain4j.model.openai.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.openai.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    OpenAiChatModel openAiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        return OpenAiChatModel.builder()
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .organizationId(chatModelProperties.organizationId())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .stop(chatModelProperties.stop())
                .maxTokens(chatModelProperties.maxTokens())
                .maxCompletionTokens(chatModelProperties.maxCompletionTokens())
                .presencePenalty(chatModelProperties.presencePenalty())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .logitBias(chatModelProperties.logitBias())
                .responseFormat(chatModelProperties.responseFormat())
                .strictJsonSchema(chatModelProperties.strictJsonSchema())
                .seed(chatModelProperties.seed())
                .user(chatModelProperties.user())
                .strictTools(chatModelProperties.strictTools())
                .parallelToolCalls(chatModelProperties.parallelToolCalls())
                .timeout(chatModelProperties.timeout())
                .maxRetries(chatModelProperties.maxRetries())
                .proxy(ProxyProperties.convert(chatModelProperties.proxy()))
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .customHeaders(chatModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    OpenAiStreamingChatModel openAiStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.streamingChatModel();
        return OpenAiStreamingChatModel.builder()
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .organizationId(chatModelProperties.organizationId())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .stop(chatModelProperties.stop())
                .maxTokens(chatModelProperties.maxTokens())
                .maxCompletionTokens(chatModelProperties.maxCompletionTokens())
                .presencePenalty(chatModelProperties.presencePenalty())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .logitBias(chatModelProperties.logitBias())
                .responseFormat(chatModelProperties.responseFormat())
                .seed(chatModelProperties.seed())
                .user(chatModelProperties.user())
                .strictTools(chatModelProperties.strictTools())
                .parallelToolCalls(chatModelProperties.parallelToolCalls())
                .timeout(chatModelProperties.timeout())
                .proxy(ProxyProperties.convert(chatModelProperties.proxy()))
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .customHeaders(chatModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.api-key")
    OpenAiLanguageModel openAiLanguageModel(Properties properties) {
        LanguageModelProperties languageModelProperties = properties.languageModel();
        return OpenAiLanguageModel.builder()
                .baseUrl(languageModelProperties.baseUrl())
                .apiKey(languageModelProperties.apiKey())
                .organizationId(languageModelProperties.organizationId())
                .modelName(languageModelProperties.modelName())
                .temperature(languageModelProperties.temperature())
                .timeout(languageModelProperties.timeout())
                .maxRetries(languageModelProperties.maxRetries())
                .proxy(ProxyProperties.convert(languageModelProperties.proxy()))
                .logRequests(languageModelProperties.logRequests())
                .logResponses(languageModelProperties.logResponses())
                .customHeaders(languageModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    OpenAiStreamingLanguageModel openAiStreamingLanguageModel(Properties properties) {
        LanguageModelProperties languageModelProperties = properties.streamingLanguageModel();
        return OpenAiStreamingLanguageModel.builder()
                .baseUrl(languageModelProperties.baseUrl())
                .apiKey(languageModelProperties.apiKey())
                .organizationId(languageModelProperties.organizationId())
                .modelName(languageModelProperties.modelName())
                .temperature(languageModelProperties.temperature())
                .timeout(languageModelProperties.timeout())
                .proxy(ProxyProperties.convert(languageModelProperties.proxy()))
                .logRequests(languageModelProperties.logRequests())
                .logResponses(languageModelProperties.logResponses())
                .customHeaders(languageModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    OpenAiEmbeddingModel openAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.embeddingModel();
        return OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.baseUrl())
                .apiKey(embeddingModelProperties.apiKey())
                .organizationId(embeddingModelProperties.organizationId())
                .modelName(embeddingModelProperties.modelName())
                .dimensions(embeddingModelProperties.dimensions())
                .maxSegmentsPerBatch(embeddingModelProperties.maxSegmentsPerBatch())
                .user(embeddingModelProperties.user())
                .timeout(embeddingModelProperties.timeout())
                .maxRetries(embeddingModelProperties.maxRetries())
                .proxy(ProxyProperties.convert(embeddingModelProperties.proxy()))
                .logRequests(embeddingModelProperties.logRequests())
                .logResponses(embeddingModelProperties.logResponses())
                .customHeaders(embeddingModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    OpenAiModerationModel openAiModerationModel(Properties properties) {
        ModerationModelProperties moderationModelProperties = properties.moderationModel();
        return OpenAiModerationModel.builder()
                .baseUrl(moderationModelProperties.baseUrl())
                .apiKey(moderationModelProperties.apiKey())
                .organizationId(moderationModelProperties.organizationId())
                .modelName(moderationModelProperties.modelName())
                .timeout(moderationModelProperties.timeout())
                .maxRetries(moderationModelProperties.maxRetries())
                .proxy(ProxyProperties.convert(moderationModelProperties.proxy()))
                .logRequests(moderationModelProperties.logRequests())
                .logResponses(moderationModelProperties.logResponses())
                .customHeaders(moderationModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    OpenAiImageModel openAiImageModel(Properties properties) {
        ImageModelProperties imageModelProperties = properties.imageModel();
        return OpenAiImageModel.builder()
                .baseUrl(imageModelProperties.baseUrl())
                .apiKey(imageModelProperties.apiKey())
                .organizationId(imageModelProperties.organizationId())
                .modelName(imageModelProperties.modelName())
                .size(imageModelProperties.size())
                .quality(imageModelProperties.quality())
                .style(imageModelProperties.style())
                .user(imageModelProperties.user())
                .responseFormat(imageModelProperties.responseFormat())
                .timeout(imageModelProperties.timeout())
                .maxRetries(imageModelProperties.maxRetries())
                .proxy(ProxyProperties.convert(imageModelProperties.proxy()))
                .logRequests(imageModelProperties.logRequests())
                .logResponses(imageModelProperties.logResponses())
                .withPersisting(imageModelProperties.withPersisting())
                .persistTo(imageModelProperties.persistTo())
                .customHeaders(imageModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    OpenAiTokenizer openAiTokenizer() {
        return new OpenAiTokenizer();
    }
}