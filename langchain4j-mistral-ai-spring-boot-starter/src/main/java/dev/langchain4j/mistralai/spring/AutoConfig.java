package dev.langchain4j.mistralai.spring;

import static dev.langchain4j.mistralai.spring.Properties.PREFIX;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiFimModel;
import dev.langchain4j.model.mistralai.MistralAiModerationModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingFimModel;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    private static final String TASK_EXECUTOR_THREAD_NAME_PREFIX = "LangChain4j-MistralAI-";

    private static final String CHAT_MODEL_HTTP_CLIENT_BUILDER = "mistralAiChatModelHttpClientBuilder";

    private static final String STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER = "mistralAiStreamingChatModelHttpClientBuilder";
    private static final String STREAMING_CHAT_MODEL_TASK_EXECUTOR = "mistralAiStreamingChatModelTaskExecutor";

    private static final String FIM_MODEL_HTTP_CLIENT_BUILDER = "mistralAiFimModelHttpClientBuilder";

    private static final String STREAMING_FIM_MODEL_HTTP_CLIENT_BUILDER = "mistralAiStreamingFimModelHttpClientBuilder";
    private static final String STREAMING_FIM_MODEL_TASK_EXECUTOR = "mistralAiStreamingFimModelTaskExecutor";

    private static final String EMBEDDING_MODEL_HTTP_CLIENT_BUILDER = "mistralAiEmbeddingModelHttpClientBuilder";

    private static final String MODERATION_MODEL_HTTP_CLIENT_BUILDER = "mistralAiModerationModelHttpClientBuilder";

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    MistralAiChatModel mistralAiChatModel(
            @Qualifier(CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        MistralAiChatModel.MistralAiChatModelBuilder builder = MistralAiChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .safePrompt(chatModelProperties.getSafePrompt())
                .randomSeed(chatModelProperties.getRandomSeed())
                .responseFormat(chatModelProperties.getResponseFormat())
                .stopSequences(chatModelProperties.getStopSequences())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList());

        // Conditional parameters to avoid NPE in Mistral AI models
        if (chatModelProperties.getMaxRetries() != null) {
            builder.maxRetries(chatModelProperties.getMaxRetries());
        }
        if (chatModelProperties.getSupportedCapabilities() != null) {
            builder.supportedCapabilities(chatModelProperties.getSupportedCapabilities());
        }

        return builder.build();
    }

    @Bean(CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    @ConditionalOnMissingBean(name = CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder mistralAiChatModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming MistralAiChatModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    MistralAiStreamingChatModel mistralAiStreamingChatModel(
            @Qualifier(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        MistralAiStreamingChatModel.MistralAiStreamingChatModelBuilder builder = MistralAiStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .safePrompt(chatModelProperties.getSafePrompt())
                .randomSeed(chatModelProperties.getRandomSeed())
                .responseFormat(chatModelProperties.getResponseFormat())
                .stopSequences(chatModelProperties.getStopSequences())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList());

        // Conditional parameters to avoid NPE in Mistral AI models
        if (chatModelProperties.getSupportedCapabilities() != null) {
            builder.supportedCapabilities(chatModelProperties.getSupportedCapabilities());
        }

        return builder.build();
    }

    @Bean(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder mistralAiStreamingChatModelHttpClientBuilder(
            ObjectProvider<RestClient.Builder> restClientBuilder,
            @Qualifier(STREAMING_CHAT_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor mistralAiStreamingChatModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor mistralAiStreamingChatModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    MistralAiEmbeddingModel mistralAiEmbeddingModel(
            @Qualifier(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return MistralAiEmbeddingModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .apiKey(embeddingModelProperties.getApiKey())
                .modelName(embeddingModelProperties.getModelName())
                .timeout(embeddingModelProperties.getTimeout())
                .logRequests(embeddingModelProperties.getLogRequests())
                .logResponses(embeddingModelProperties.getLogResponses())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .build();
    }

    @Bean(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    @ConditionalOnMissingBean(name = EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder mistralAiEmbeddingModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming MistralAiEmbeddingModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".fim-model.api-key")
    MistralAiFimModel mistralAiFimModel(
            @Qualifier(FIM_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties) {
        FimModelProperties fimModelProperties = properties.getFimModel();
        return MistralAiFimModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(fimModelProperties.getBaseUrl())
                .apiKey(fimModelProperties.getApiKey())
                .modelName(fimModelProperties.getModelName())
                .temperature(fimModelProperties.getTemperature())
                .maxTokens(fimModelProperties.getMaxTokens())
                .minTokens(fimModelProperties.getMinTokens())
                .topP(fimModelProperties.getTopP())
                .randomSeed(fimModelProperties.getRandomSeed())
                .stop(fimModelProperties.getStop())
                .timeout(fimModelProperties.getTimeout())
                .logRequests(fimModelProperties.getLogRequests())
                .logResponses(fimModelProperties.getLogResponses())
                .maxRetries(fimModelProperties.getMaxRetries())
                .build();
    }

    @Bean(FIM_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".fim-model.api-key")
    @ConditionalOnMissingBean(name = FIM_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder mistralAiFimModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming MistralAiFimModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-fim-model.api-key")
    MistralAiStreamingFimModel mistralAiStreamingFimModel(
            @Qualifier(STREAMING_FIM_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties) {
        FimModelProperties fimModelProperties = properties.getStreamingFimModel();
        return MistralAiStreamingFimModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(fimModelProperties.getBaseUrl())
                .apiKey(fimModelProperties.getApiKey())
                .modelName(fimModelProperties.getModelName())
                .temperature(fimModelProperties.getTemperature())
                .maxTokens(fimModelProperties.getMaxTokens())
                .minTokens(fimModelProperties.getMinTokens())
                .topP(fimModelProperties.getTopP())
                .randomSeed(fimModelProperties.getRandomSeed())
                .stop(fimModelProperties.getStop())
                .timeout(fimModelProperties.getTimeout())
                .logRequests(fimModelProperties.getLogRequests())
                .logResponses(fimModelProperties.getLogResponses())
                .build();
    }

    @Bean(STREAMING_FIM_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-fim-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_FIM_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder mistralAiStreamingFimModelHttpClientBuilder(
            ObjectProvider<RestClient.Builder> restClientBuilder,
            @Qualifier(STREAMING_FIM_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_FIM_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-fim-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_FIM_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor mistralAiStreamingFimModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_FIM_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-fim-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_FIM_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor mistralAiStreamingFimModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    MistralAiModerationModel mistralAiModerationModel(
            @Qualifier(MODERATION_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties) {
        ModerationModelProperties moderationModelProperties = properties.getModerationModel();
        MistralAiModerationModel.Builder builder = new MistralAiModerationModel.Builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(moderationModelProperties.getBaseUrl())
                .apiKey(moderationModelProperties.getApiKey())
                .modelName(moderationModelProperties.getModelName())
                .timeout(moderationModelProperties.getTimeout())
                .logRequests(moderationModelProperties.getLogRequests())
                .logResponses(moderationModelProperties.getLogResponses());

        // Conditional parameter to avoid NPE in Mistral AI models
        if (moderationModelProperties.getMaxRetries() != null) {
            builder.maxRetries(moderationModelProperties.getMaxRetries());
        }

        return builder.build();
    }

    @Bean(MODERATION_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    @ConditionalOnMissingBean(name = MODERATION_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder mistralAiModerationModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming MistralAiModerationModel
                .createDefaultStreamingRequestExecutor(false);
    }
}
