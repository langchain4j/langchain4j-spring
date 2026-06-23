package dev.langchain4j.azure.openai.spring;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import dev.langchain4j.model.azure.*;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.azure.ai.openai.models.ReasoningEffortValue;

import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".chat-model.api-key")
    AzureOpenAiChatModel openAiChatModelByAPIKey(Properties properties,
                                                 ObjectProvider<ChatModelListener> listeners) {
        return openAiChatModel(properties, listeners);
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".chat-model.non-azure-api-key")
    AzureOpenAiChatModel openAiChatModelByNonAzureApiKey(Properties properties,
                                                         ObjectProvider<ChatModelListener> listeners) {
        return openAiChatModel(properties, listeners);
    }

    AzureOpenAiChatModel openAiChatModel(Properties properties, ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint(chatModelProperties.endpoint())
                .serviceVersion(chatModelProperties.serviceVersion())
                .apiKey(chatModelProperties.apiKey())
                .deploymentName(chatModelProperties.deploymentName())
                .maxTokens(chatModelProperties.maxTokens())
                .maxCompletionTokens(chatModelProperties.maxCompletionTokens())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .logitBias(chatModelProperties.logitBias())
                .user(chatModelProperties.user())
                .stop(chatModelProperties.stop())
                .presencePenalty(chatModelProperties.presencePenalty())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .seed(chatModelProperties.seed())
                .strictJsonSchema(chatModelProperties.strictJsonSchema())
                .timeout(Duration.ofSeconds(chatModelProperties.timeout() == null ? 0 : chatModelProperties.timeout()))
                .maxRetries(chatModelProperties.maxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses() != null && chatModelProperties.logRequestsAndResponses())
                .userAgentSuffix(chatModelProperties.userAgentSuffix())
                .listeners(listeners.orderedStream().toList())
                .customHeaders(chatModelProperties.customHeaders())
                .supportedCapabilities(chatModelProperties.supportedCapabilities());
        if (chatModelProperties.nonAzureApiKey() != null) {
            builder.nonAzureApiKey(chatModelProperties.nonAzureApiKey());
        }
        if(chatModelProperties.reasoningEffort() != null && !chatModelProperties.reasoningEffort().isEmpty()) {
            builder.reasoningEffort(ReasoningEffortValue.fromString(chatModelProperties.reasoningEffort()));
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-chat-model.api-key")
    AzureOpenAiStreamingChatModel openAiStreamingChatModelByApiKey(Properties properties,
                                                                   ObjectProvider<ChatModelListener> listeners) {
        return openAiStreamingChatModel(properties, listeners);
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-chat-model.non-azure-api-key")
    AzureOpenAiStreamingChatModel openAiStreamingChatModelByNonAzureApiKey(Properties properties,
                                                                           ObjectProvider<ChatModelListener> listeners) {
        return openAiStreamingChatModel(properties, listeners);
    }

    AzureOpenAiStreamingChatModel openAiStreamingChatModel(Properties properties,
                                                           ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.streamingChatModel();
        AzureOpenAiStreamingChatModel.Builder builder = AzureOpenAiStreamingChatModel.builder()
                .endpoint(chatModelProperties.endpoint())
                .serviceVersion(chatModelProperties.serviceVersion())
                .apiKey(chatModelProperties.apiKey())
                .deploymentName(chatModelProperties.deploymentName())
                .maxTokens(chatModelProperties.maxTokens())
                .maxCompletionTokens(chatModelProperties.maxCompletionTokens())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .logitBias(chatModelProperties.logitBias())
                .user(chatModelProperties.user())
                .stop(chatModelProperties.stop())
                .presencePenalty(chatModelProperties.presencePenalty())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .seed(chatModelProperties.seed())
                .timeout(Duration.ofSeconds(chatModelProperties.timeout() == null ? 0 : chatModelProperties.timeout()))
                .maxRetries(chatModelProperties.maxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses() != null && chatModelProperties.logRequestsAndResponses())
                .userAgentSuffix(chatModelProperties.userAgentSuffix())
                .listeners(listeners.orderedStream().toList())
                .customHeaders(chatModelProperties.customHeaders())
                .supportedCapabilities(chatModelProperties.supportedCapabilities());
        if (chatModelProperties.nonAzureApiKey() != null) {
            builder.nonAzureApiKey(chatModelProperties.nonAzureApiKey());
        }
        if(chatModelProperties.reasoningEffort() != null && !chatModelProperties.reasoningEffort().isEmpty()) {
            builder.reasoningEffort(ReasoningEffortValue.fromString(chatModelProperties.reasoningEffort()));
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty({Properties.PREFIX + ".embedding-model.api-key"})
    AzureOpenAiEmbeddingModel openAiEmbeddingModelByApiKey(Properties properties) {
        return openAiEmbeddingModel(properties);
    }

    @Bean
    @ConditionalOnProperty({Properties.PREFIX + ".embedding-model.non-azure-api-key"})
    AzureOpenAiEmbeddingModel openAiEmbeddingModelByNonAzureApiKey(Properties properties) {
        return openAiEmbeddingModel(properties);
    }

    AzureOpenAiEmbeddingModel openAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.embeddingModel();
        AzureOpenAiEmbeddingModel.Builder builder = AzureOpenAiEmbeddingModel.builder()
                .endpoint(embeddingModelProperties.endpoint())
                .serviceVersion(embeddingModelProperties.serviceVersion())
                .apiKey(embeddingModelProperties.apiKey())
                .deploymentName(embeddingModelProperties.deploymentName())
                .timeout(Duration.ofSeconds(embeddingModelProperties.timeout() == null ? 0 : embeddingModelProperties.timeout()))
                .maxRetries(embeddingModelProperties.maxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(embeddingModelProperties.logRequestsAndResponses() != null && embeddingModelProperties.logRequestsAndResponses())
                .userAgentSuffix(embeddingModelProperties.userAgentSuffix())
                .dimensions(embeddingModelProperties.dimensions())
                .customHeaders(embeddingModelProperties.customHeaders());
        if (embeddingModelProperties.nonAzureApiKey() != null) {
            builder.nonAzureApiKey(embeddingModelProperties.nonAzureApiKey());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".image-model.api-key")
    AzureOpenAiImageModel openAiImageModelByApiKey(Properties properties) {
        return openAiImageModel(properties);
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".image-model.non-azure-api-key")
    AzureOpenAiImageModel openAiImageModelByNonAzureApiKey(Properties properties) {
        return openAiImageModel(properties);
    }

    AzureOpenAiImageModel openAiImageModel(Properties properties) {
        ImageModelProperties imageModelProperties = properties.imageModel();
        AzureOpenAiImageModel.Builder builder = AzureOpenAiImageModel.builder()
                .endpoint(imageModelProperties.endpoint())
                .serviceVersion(imageModelProperties.serviceVersion())
                .apiKey(imageModelProperties.apiKey())
                .deploymentName(imageModelProperties.deploymentName())
                .quality(imageModelProperties.quality())
                .size(imageModelProperties.size())
                .user(imageModelProperties.user())
                .style(imageModelProperties.style())
                .responseFormat(imageModelProperties.responseFormat())
                .timeout(imageModelProperties.timeout() == null ? null : Duration.ofSeconds(imageModelProperties.timeout()))
                .maxRetries(imageModelProperties.maxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(imageModelProperties.logRequestsAndResponses() != null && imageModelProperties.logRequestsAndResponses())
                .userAgentSuffix(imageModelProperties.userAgentSuffix())
                .customHeaders(imageModelProperties.customHeaders());
        if (imageModelProperties.nonAzureApiKey() != null) {
            builder.nonAzureApiKey(imageModelProperties.nonAzureApiKey());
        }
        return builder.build();
    }
}
