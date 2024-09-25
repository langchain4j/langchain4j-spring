package dev.langchain4j.model.githubmodels.spring;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsEmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".chat-model.github-token")
    GitHubModelsChatModel gitHubModelsChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        GitHubModelsChatModel.Builder builder = GitHubModelsChatModel.builder()
                .endpoint(chatModelProperties.getEndpoint())
                .gitHubToken(chatModelProperties.getGitHubToken())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses());

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(Properties.PREFIX + ".streaming-chat-model.github-token")
    GitHubModelsStreamingChatModel gitHubModelsStreamingChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        GitHubModelsStreamingChatModel.Builder builder = GitHubModelsStreamingChatModel.builder()
                .endpoint(chatModelProperties.getEndpoint())
                .gitHubToken(chatModelProperties.getGitHubToken())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .stop(chatModelProperties.getStop())
                .maxTokens(chatModelProperties.getMaxTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(chatModelProperties.getTimeout())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses());

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty({Properties.PREFIX + ".embedding-model.github-token"})
    GitHubModelsEmbeddingModel openAiEmbeddingModelByApiKey(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        GitHubModelsEmbeddingModel.Builder builder = GitHubModelsEmbeddingModel.builder()
                .endpoint(embeddingModelProperties.getEndpoint())
                .gitHubToken(embeddingModelProperties.getGitHubToken())
                .modelName(embeddingModelProperties.getModelName())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .timeout(embeddingModelProperties.getTimeout())
                .proxyOptions(ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration()))
                .logRequestsAndResponses(embeddingModelProperties.getLogRequestsAndResponses() != null && embeddingModelProperties.getLogRequestsAndResponses());

        return builder.build();
    }
}