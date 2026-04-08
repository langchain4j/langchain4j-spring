package dev.langchain4j.model.githubmodels.spring;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsEmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(GitHubModelsProperties.class)
public class GitHubModelsAutoConfiguration {

    @Bean
    @ConditionalOnProperty(GitHubModelsProperties.PREFIX + ".chat-model.github-token")
    GitHubModelsChatModel gitHubModelsChatModel(GitHubModelsProperties properties, ObjectProvider<ChatModelListener> listeners) {
        GitHubModelsChatModelProperties chatModelProperties = properties.getChatModel();
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
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses())
                .listeners(listeners.orderedStream().toList());

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(GitHubModelsProperties.PREFIX + ".streaming-chat-model.github-token")
    GitHubModelsStreamingChatModel gitHubModelsStreamingChatModel(GitHubModelsProperties properties,
                                                                  ObjectProvider<ChatModelListener> listeners) {
        GitHubModelsChatModelProperties chatModelProperties = properties.getStreamingChatModel();
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
                .logRequestsAndResponses(chatModelProperties.getLogRequestsAndResponses() != null && chatModelProperties.getLogRequestsAndResponses())
                .listeners(listeners.orderedStream().toList());

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty({GitHubModelsProperties.PREFIX + ".embedding-model.github-token"})
    GitHubModelsEmbeddingModel openAiEmbeddingModelByApiKey(GitHubModelsProperties properties) {
        GitHubModelsEmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
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