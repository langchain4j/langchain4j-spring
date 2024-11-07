package dev.langchain4j.azure.openai.spring;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.azure.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".chat-model.api-key")
    AzureOpenAiChatModel openAiChatModelByAPIKey(Properties properties) {
        return openAiChatModel(properties);
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".chat-model.non-azure-api-key")
    AzureOpenAiChatModel openAiChatModelByNonAzureApiKey(Properties properties) {
        return openAiChatModel(properties);
    }

    AzureOpenAiChatModel openAiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder()
                .endpoint(chatModelProperties.getEndpoint())
                .serviceVersion(chatModelProperties.getServiceVersion())
                .apiKey(chatModelProperties.getApiKey())
                .deploymentName(chatModelProperties.getDeploymentName())
                // TODO inject tokenizer?
                .maxTokens(chatModelProperties.getMaxTokens())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .logitBias(chatModelProperties.getLogitBias())
                .user(chatModelProperties.getUser())
                .stop(chatModelProperties.getStop())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .seed(chatModelProperties.getSeed())
                .timeout(Duration.ofSeconds(chatModelProperties.getTimeout() == null ? 0 : chatModelProperties.getTimeout()))
                .maxRetries(chatModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses())
                .userAgentSuffix(chatModelProperties.getUserAgentSuffix())
                .customHeaders(chatModelProperties.getCustomHeaders());
        if (chatModelProperties.getNonAzureApiKey() != null) {
            builder.nonAzureApiKey(chatModelProperties.getNonAzureApiKey());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-chat-model.api-key")
    AzureOpenAiStreamingChatModel openAiStreamingChatModelByApiKey(Properties properties) {
        return openAiStreamingChatModel(properties);
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-chat-model.non-azure-api-key")
    AzureOpenAiStreamingChatModel openAiStreamingChatModelByNonAzureApiKey(Properties properties) {
        return openAiStreamingChatModel(properties);
    }

    AzureOpenAiStreamingChatModel openAiStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        AzureOpenAiStreamingChatModel.Builder builder = AzureOpenAiStreamingChatModel.builder()
                .endpoint(chatModelProperties.getEndpoint())
                .serviceVersion(chatModelProperties.getServiceVersion())
                .apiKey(chatModelProperties.getApiKey())
                .deploymentName(chatModelProperties.getDeploymentName())
                // TODO inject tokenizer?
                .maxTokens(chatModelProperties.getMaxTokens())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .logitBias(chatModelProperties.getLogitBias())
                .user(chatModelProperties.getUser())
                .stop(chatModelProperties.getStop())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .seed(chatModelProperties.getSeed())
                .timeout(Duration.ofSeconds(chatModelProperties.getTimeout() == null ? 0 : chatModelProperties.getTimeout()))
                .maxRetries(chatModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses())
                .userAgentSuffix(chatModelProperties.getUserAgentSuffix())
                .customHeaders(chatModelProperties.getCustomHeaders());
        if (chatModelProperties.getNonAzureApiKey() != null) {
            builder.nonAzureApiKey(chatModelProperties.getNonAzureApiKey());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty({Properties.PREFIX + ".embedding-model.api-key"})
    AzureOpenAiEmbeddingModel openAiEmbeddingModelByApiKey(Properties properties, @Nullable Tokenizer tokenizer) {
        return openAiEmbeddingModel(properties, tokenizer);
    }

    @Bean
    @ConditionalOnProperty({Properties.PREFIX + ".embedding-model.non-azure-api-key"})
    AzureOpenAiEmbeddingModel openAiEmbeddingModelByNonAzureApiKey(Properties properties, @Nullable Tokenizer tokenizer) {
        return openAiEmbeddingModel(properties, tokenizer);
    }

    AzureOpenAiEmbeddingModel openAiEmbeddingModel(Properties properties, Tokenizer tokenizer) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        AzureOpenAiEmbeddingModel.Builder builder = AzureOpenAiEmbeddingModel.builder()
                .endpoint(embeddingModelProperties.getEndpoint())
                .serviceVersion(embeddingModelProperties.getServiceVersion())
                .apiKey(embeddingModelProperties.getApiKey())
                .deploymentName(embeddingModelProperties.getDeploymentName())
                .tokenizer(tokenizer)
                .timeout(Duration.ofSeconds(embeddingModelProperties.getTimeout() == null ? 0 : embeddingModelProperties.getTimeout()))
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(embeddingModelProperties.getLogRequestsAndResponses() != null && embeddingModelProperties.getLogRequestsAndResponses())
                .userAgentSuffix(embeddingModelProperties.getUserAgentSuffix())
                .dimensions(embeddingModelProperties.getDimensions())
                .customHeaders(embeddingModelProperties.getCustomHeaders());
        if (embeddingModelProperties.getNonAzureApiKey() != null) {
            builder.nonAzureApiKey(embeddingModelProperties.getNonAzureApiKey());
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
        ImageModelProperties imageModelProperties = properties.getImageModel();
        AzureOpenAiImageModel.Builder builder = AzureOpenAiImageModel.builder()
                .endpoint(imageModelProperties.getEndpoint())
                .serviceVersion(imageModelProperties.getServiceVersion())
                .apiKey(imageModelProperties.getApiKey())
                .deploymentName(imageModelProperties.getDeploymentName())
                .quality(imageModelProperties.getQuality())
                .size(imageModelProperties.getSize())
                .user(imageModelProperties.getUser())
                .style(imageModelProperties.getStyle())
                .responseFormat(imageModelProperties.getResponseFormat())
                .timeout(imageModelProperties.getTimeout() == null ? null : Duration.ofSeconds(imageModelProperties.getTimeout()))
                .maxRetries(imageModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(imageModelProperties.getLogRequestsAndResponses() != null && imageModelProperties.getLogRequestsAndResponses())
                .userAgentSuffix(imageModelProperties.getUserAgentSuffix())
                .customHeaders(imageModelProperties.getCustomHeaders());
        if (imageModelProperties.getNonAzureApiKey() != null) {
            builder.nonAzureApiKey(imageModelProperties.getNonAzureApiKey());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    AzureOpenAiTokenizer azureOpenAiTokenizer() {
        return new AzureOpenAiTokenizer();
    }
}