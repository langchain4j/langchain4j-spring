package dev.langchain4j.mistralai.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiFimModel;
import dev.langchain4j.model.mistralai.MistralAiModerationModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingFimModel;
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

import static dev.langchain4j.mistralai.spring.Properties.PREFIX;

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
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .maxTokens(chatModelProperties.maxTokens())
                .safePrompt(chatModelProperties.safePrompt())
                .randomSeed(chatModelProperties.randomSeed())
                .responseFormat(chatModelProperties.responseFormat())
                .stopSequences(chatModelProperties.stopSequences())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .presencePenalty(chatModelProperties.presencePenalty())
                .timeout(chatModelProperties.timeout())
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .listeners(listeners.orderedStream().toList());

        // Conditional parameters to avoid NPE in Mistral AI models
        if (chatModelProperties.maxRetries() != null) {
            builder.maxRetries(chatModelProperties.maxRetries());
        }
        if (chatModelProperties.supportedCapabilities() != null) {
            builder.supportedCapabilities(chatModelProperties.supportedCapabilities());
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
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .maxTokens(chatModelProperties.maxTokens())
                .safePrompt(chatModelProperties.safePrompt())
                .randomSeed(chatModelProperties.randomSeed())
                .responseFormat(chatModelProperties.responseFormat())
                .stopSequences(chatModelProperties.stopSequences())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .presencePenalty(chatModelProperties.presencePenalty())
                .timeout(chatModelProperties.timeout())
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .listeners(listeners.orderedStream().toList());

        // Conditional parameters to avoid NPE in Mistral AI models
        if (chatModelProperties.supportedCapabilities() != null) {
            builder.supportedCapabilities(chatModelProperties.supportedCapabilities());
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
                .baseUrl(embeddingModelProperties.baseUrl())
                .apiKey(embeddingModelProperties.apiKey())
                .modelName(embeddingModelProperties.modelName())
                .timeout(embeddingModelProperties.timeout())
                .logRequests(embeddingModelProperties.logRequests())
                .logResponses(embeddingModelProperties.logResponses())
                .maxRetries(embeddingModelProperties.maxRetries())
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
                .baseUrl(fimModelProperties.baseUrl())
                .apiKey(fimModelProperties.apiKey())
                .modelName(fimModelProperties.modelName())
                .temperature(fimModelProperties.temperature())
                .maxTokens(fimModelProperties.maxTokens())
                .minTokens(fimModelProperties.minTokens())
                .topP(fimModelProperties.topP())
                .randomSeed(fimModelProperties.randomSeed())
                .stop(fimModelProperties.stop())
                .timeout(fimModelProperties.timeout())
                .logRequests(fimModelProperties.logRequests())
                .logResponses(fimModelProperties.logResponses())
                .maxRetries(fimModelProperties.maxRetries())
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
                .baseUrl(fimModelProperties.baseUrl())
                .apiKey(fimModelProperties.apiKey())
                .modelName(fimModelProperties.modelName())
                .temperature(fimModelProperties.temperature())
                .maxTokens(fimModelProperties.maxTokens())
                .minTokens(fimModelProperties.minTokens())
                .topP(fimModelProperties.topP())
                .randomSeed(fimModelProperties.randomSeed())
                .stop(fimModelProperties.stop())
                .timeout(fimModelProperties.timeout())
                .logRequests(fimModelProperties.logRequests())
                .logResponses(fimModelProperties.logResponses())
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
                .baseUrl(moderationModelProperties.baseUrl())
                .apiKey(moderationModelProperties.apiKey())
                .modelName(moderationModelProperties.modelName())
                .timeout(moderationModelProperties.timeout())
                .logRequests(moderationModelProperties.logRequests())
                .logResponses(moderationModelProperties.logResponses());

        // Conditional parameter to avoid NPE in Mistral AI models
        if (moderationModelProperties.maxRetries() != null) {
            builder.maxRetries(moderationModelProperties.maxRetries());
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
