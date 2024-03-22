package dev.langchain4j.azure.openai.spring;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.azure.openai.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    AzureOpenAiChatModel openAiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return AzureOpenAiChatModel.builder()
                .endpoint(chatModelProperties.getEndpoint())
                .apiKey(chatModelProperties.getApiKey())
                .deploymentName(chatModelProperties.getDeploymentName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .stop(chatModelProperties.getStop())
                .maxTokens(chatModelProperties.getMaxTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    AzureOpenAiStreamingChatModel openAiStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return AzureOpenAiStreamingChatModel.builder()
                .endpoint(chatModelProperties.getEndpoint())
                .apiKey(chatModelProperties.getApiKey())
                .deploymentName(chatModelProperties.getDeploymentName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .stop(chatModelProperties.getStop())
                .maxTokens(chatModelProperties.getMaxTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(chatModelProperties.getTimeout())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    AzureOpenAiEmbeddingModel openAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return AzureOpenAiEmbeddingModel.builder()
                .endpoint(embeddingModelProperties.getEndpoint())
                .apiKey(embeddingModelProperties.getApiKey())
                .deploymentName(embeddingModelProperties.getDeploymentName())
                .timeout(embeddingModelProperties.getTimeout())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(embeddingModelProperties.getLogRequestsAndResponses() != null && embeddingModelProperties.getLogRequestsAndResponses())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    AzureOpenAiImageModel openAiImageModel(Properties properties) {
        ImageModelProperties imageModelProperties = properties.getImageModel();
        return AzureOpenAiImageModel.builder()
                .endpoint(imageModelProperties.getEndpoint())
                .apiKey(imageModelProperties.getApiKey())
                .deploymentName(imageModelProperties.getDeploymentName())
                .size(imageModelProperties.getSize())
                .quality(imageModelProperties.getQuality())
                .style(imageModelProperties.getStyle())
                .user(imageModelProperties.getUser())
                .responseFormat(imageModelProperties.getResponseFormat())
                .timeout(imageModelProperties.getTimeout())
                .maxRetries(imageModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(imageModelProperties.getLogRequestsAndResponses() != null && imageModelProperties.getLogRequestsAndResponses())
                .build();
    }
}