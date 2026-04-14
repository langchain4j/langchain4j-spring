package dev.langchain4j.openaiofficial.spring;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatRequestParameters;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialEmbeddingModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialImageModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialStreamingChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.openaiofficial.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    @ConditionalOnMissingBean
    OpenAiOfficialChatModel openAiOfficialChatModel(Properties properties,
                                                     ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        return OpenAiOfficialChatModel.builder()
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .organizationId(chatModelProperties.organizationId())
                .isMicrosoftFoundry(Boolean.TRUE.equals(chatModelProperties.isMicrosoftFoundry()))
                .microsoftFoundryDeploymentName(chatModelProperties.microsoftFoundryDeploymentName())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .stop(chatModelProperties.stop())
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
                .store(chatModelProperties.store())
                .metadata(chatModelProperties.metadata())
                .serviceTier(chatModelProperties.serviceTier())
                .supportedCapabilities(chatModelProperties.supportedCapabilities())
                .defaultRequestParameters(OpenAiOfficialChatRequestParameters.builder()
                        .reasoningEffort(chatModelProperties.reasoningEffort())
                        .build())
                .timeout(chatModelProperties.timeout())
                .maxRetries(chatModelProperties.maxRetries())
                .customHeaders(chatModelProperties.customHeaders())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean
    OpenAiOfficialStreamingChatModel openAiOfficialStreamingChatModel(Properties properties,
                                                                      ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.streamingChatModel();
        return OpenAiOfficialStreamingChatModel.builder()
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .organizationId(chatModelProperties.organizationId())
                .isMicrosoftFoundry(Boolean.TRUE.equals(chatModelProperties.isMicrosoftFoundry()))
                .microsoftFoundryDeploymentName(chatModelProperties.microsoftFoundryDeploymentName())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .stop(chatModelProperties.stop())
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
                .store(chatModelProperties.store())
                .metadata(chatModelProperties.metadata())
                .serviceTier(chatModelProperties.serviceTier())
                .supportedCapabilities(chatModelProperties.supportedCapabilities())
                .defaultRequestParameters(OpenAiOfficialChatRequestParameters.builder()
                        .reasoningEffort(chatModelProperties.reasoningEffort())
                        .build())
                .timeout(chatModelProperties.timeout())
                .maxRetries(chatModelProperties.maxRetries())
                .customHeaders(chatModelProperties.customHeaders())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    @ConditionalOnMissingBean
    OpenAiOfficialEmbeddingModel openAiOfficialEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.embeddingModel();
        return OpenAiOfficialEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.baseUrl())
                .apiKey(embeddingModelProperties.apiKey())
                .organizationId(embeddingModelProperties.organizationId())
                .modelName(embeddingModelProperties.modelName())
                .dimensions(embeddingModelProperties.dimensions())
                .user(embeddingModelProperties.user())
                .maxSegmentsPerBatch(embeddingModelProperties.maxSegmentsPerBatch())
                .timeout(embeddingModelProperties.timeout())
                .maxRetries(embeddingModelProperties.maxRetries())
                .customHeaders(embeddingModelProperties.customHeaders())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    @ConditionalOnMissingBean
    OpenAiOfficialImageModel openAiOfficialImageModel(Properties properties) {
        ImageModelProperties imageModelProperties = properties.imageModel();
        return OpenAiOfficialImageModel.builder()
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
                .customHeaders(imageModelProperties.customHeaders())
                .build();
    }
}
