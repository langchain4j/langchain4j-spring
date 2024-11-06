package dev.langchain4j.azure.openai.spring;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.azure.AzureOpenAiTokenizer;
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
                .apiKey(chatModelProperties.getApiKey())
                .deploymentName(chatModelProperties.getDeploymentName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(Duration.ofSeconds(chatModelProperties.getTimeout() == null ? 0 : chatModelProperties.getTimeout()))
                .maxRetries(chatModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses());
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
                .apiKey(chatModelProperties.getApiKey())
                .deploymentName(chatModelProperties.getDeploymentName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .stop(chatModelProperties.getStop())
                .maxTokens(chatModelProperties.getMaxTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(Duration.ofSeconds(chatModelProperties.getTimeout() == null ? 0 : chatModelProperties.getTimeout()))
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses());
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
                .apiKey(embeddingModelProperties.getApiKey())
                .deploymentName(embeddingModelProperties.getDeploymentName())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .tokenizer(tokenizer)
                .timeout(Duration.ofSeconds(embeddingModelProperties.getTimeout() == null ? 0 : embeddingModelProperties.getTimeout()))
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .customHeaders(embeddingModelProperties.getCustomHeaders())
                .logRequestsAndResponses(embeddingModelProperties.getLogRequestsAndResponses() != null && embeddingModelProperties.getLogRequestsAndResponses());

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
                .apiKey(imageModelProperties.getApiKey())
                .deploymentName(imageModelProperties.getDeploymentName())
                .size(imageModelProperties.getSize())
                .quality(imageModelProperties.getQuality())
                .style(imageModelProperties.getStyle())
                .user(imageModelProperties.getUser())
                .responseFormat(imageModelProperties.getResponseFormat())
                .timeout(imageModelProperties.getTimeout() == null ? null : Duration.ofSeconds(imageModelProperties.getTimeout()))
                .maxRetries(imageModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .customHeaders(imageModelProperties.getCustomHeaders())
                .logRequestsAndResponses(imageModelProperties.getLogRequestsAndResponses() != null && imageModelProperties.getLogRequestsAndResponses());
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