package dev.langchain4j.openai.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import static dev.langchain4j.openai.spring.Properties.PREFIX;

@AutoConfiguration(after = RestClientAutoConfiguration.class)
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    private static final String TASK_EXECUTOR_THREAD_NAME_PREFIX = "LangChain4j-OpenAI-";

    private static final String CHAT_MODEL_HTTP_CLIENT_BUILDER = "openAiChatModelHttpClientBuilder";

    private static final String STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER = "openAiStreamingChatModelHttpClientBuilder";
    private static final String STREAMING_CHAT_MODEL_TASK_EXECUTOR = "openAiStreamingChatModelTaskExecutor";

    private static final String LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "openAiLanguageModelHttpClientBuilder";

    private static final String STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "openAiStreamingLanguageModelHttpClientBuilder";
    private static final String STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR = "openAiStreamingLanguageModelTaskExecutor";

    private static final String EMBEDDING_MODEL_HTTP_CLIENT_BUILDER = "openAiEmbeddingModelHttpClientBuilder";

    private static final String MODERATION_MODEL_HTTP_CLIENT_BUILDER = "openAiModerationModelHttpClientBuilder";

    private static final String IMAGE_MODEL_HTTP_CLIENT_BUILDER = "openAiImageModelHttpClientBuilder";

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    OpenAiChatModel openAiChatModel(
            @Qualifier(CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        return OpenAiChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .organizationId(chatModelProperties.getOrganizationId())
                .projectId(chatModelProperties.getProjectId())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .stop(chatModelProperties.getStop())
                .maxTokens(chatModelProperties.getMaxTokens())
                .maxCompletionTokens(chatModelProperties.getMaxCompletionTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .logitBias(chatModelProperties.getLogitBias())
                .responseFormat(chatModelProperties.getResponseFormat())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .strictJsonSchema(chatModelProperties.getStrictJsonSchema())
                .seed(chatModelProperties.getSeed())
                .user(chatModelProperties.getUser())
                .strictTools(chatModelProperties.getStrictTools())
                .parallelToolCalls(chatModelProperties.getParallelToolCalls())
                .store(chatModelProperties.getStore())
                .metadata(chatModelProperties.getMetadata())
                .serviceTier(chatModelProperties.getServiceTier())
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .reasoningEffort(chatModelProperties.getReasoningEffort())
                        .build())
                .returnThinking(chatModelProperties.getReturnThinking())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean(CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    @ConditionalOnMissingBean(name = CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiChatModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiChatModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    OpenAiStreamingChatModel openAiStreamingChatModel(
            @Qualifier(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        ChatModelProperties chatModelProperties = properties.streamingChatModel();
        return OpenAiStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .organizationId(chatModelProperties.getOrganizationId())
                .projectId(chatModelProperties.getProjectId())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .stop(chatModelProperties.getStop())
                .maxTokens(chatModelProperties.getMaxTokens())
                .maxCompletionTokens(chatModelProperties.getMaxCompletionTokens())
                .presencePenalty(chatModelProperties.getPresencePenalty())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .logitBias(chatModelProperties.getLogitBias())
                .responseFormat(chatModelProperties.getResponseFormat())
                .seed(chatModelProperties.getSeed())
                .user(chatModelProperties.getUser())
                .strictTools(chatModelProperties.getStrictTools())
                .parallelToolCalls(chatModelProperties.getParallelToolCalls())
                .store(chatModelProperties.getStore())
                .metadata(chatModelProperties.getMetadata())
                .serviceTier(chatModelProperties.getServiceTier())
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .reasoningEffort(chatModelProperties.getReasoningEffort())
                        .build())
                .returnThinking(chatModelProperties.getReturnThinking())
                .timeout(chatModelProperties.getTimeout())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiStreamingChatModelHttpClientBuilder(
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
    AsyncTaskExecutor openAiStreamingChatModelTaskExecutorWithContextPropagation() {
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
    AsyncTaskExecutor openAiStreamingChatModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.api-key")
    OpenAiLanguageModel openAiLanguageModel(
            @Qualifier(LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        LanguageModelProperties languageModelProperties = properties.languageModel();
        return OpenAiLanguageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(languageModelProperties.baseUrl())
                .apiKey(languageModelProperties.apiKey())
                .organizationId(languageModelProperties.organizationId())
                .projectId(languageModelProperties.projectId())
                .modelName(languageModelProperties.modelName())
                .temperature(languageModelProperties.temperature())
                .timeout(languageModelProperties.timeout())
                .maxRetries(languageModelProperties.maxRetries())
                .logRequests(languageModelProperties.logRequests())
                .logResponses(languageModelProperties.logResponses())
                .customHeaders(languageModelProperties.customHeaders())
                .build();
    }

    @Bean(LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".language-model.api-key")
    @ConditionalOnMissingBean(name = LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiLanguageModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiLanguageModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    OpenAiStreamingLanguageModel openAiStreamingLanguageModel(
            @Qualifier(STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        LanguageModelProperties languageModelProperties = properties.streamingLanguageModel();
        return OpenAiStreamingLanguageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(languageModelProperties.baseUrl())
                .apiKey(languageModelProperties.apiKey())
                .organizationId(languageModelProperties.organizationId())
                .projectId(languageModelProperties.projectId())
                .modelName(languageModelProperties.modelName())
                .temperature(languageModelProperties.temperature())
                .timeout(languageModelProperties.timeout())
                .logRequests(languageModelProperties.logRequests())
                .logResponses(languageModelProperties.logResponses())
                .customHeaders(languageModelProperties.customHeaders())
                .build();
    }

    @Bean(STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiStreamingLanguageModelHttpClientBuilder(
            @Qualifier(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor,
            ObjectProvider<RestClient.Builder> restClientBuilder
    ) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor openAiStreamingLanguageModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor openAiStreamingLanguageModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    OpenAiEmbeddingModel openAiEmbeddingModel(
            @Qualifier(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        EmbeddingModelProperties embeddingModelProperties = properties.embeddingModel();
        return OpenAiEmbeddingModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(embeddingModelProperties.baseUrl())
                .apiKey(embeddingModelProperties.apiKey())
                .organizationId(embeddingModelProperties.organizationId())
                .projectId(embeddingModelProperties.projectId())
                .modelName(embeddingModelProperties.modelName())
                .dimensions(embeddingModelProperties.dimensions())
                .maxSegmentsPerBatch(embeddingModelProperties.maxSegmentsPerBatch())
                .user(embeddingModelProperties.user())
                .timeout(embeddingModelProperties.timeout())
                .maxRetries(embeddingModelProperties.maxRetries())
                .logRequests(embeddingModelProperties.logRequests())
                .logResponses(embeddingModelProperties.logResponses())
                .customHeaders(embeddingModelProperties.customHeaders())
                .build();
    }

    @Bean(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    @ConditionalOnMissingBean(name = EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiEmbeddingModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiEmbeddingModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    OpenAiModerationModel openAiModerationModel(
            @Qualifier(MODERATION_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        ModerationModelProperties moderationModelProperties = properties.moderationModel();
        return OpenAiModerationModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(moderationModelProperties.baseUrl())
                .apiKey(moderationModelProperties.apiKey())
                .organizationId(moderationModelProperties.organizationId())
                .projectId(moderationModelProperties.projectId())
                .modelName(moderationModelProperties.modelName())
                .timeout(moderationModelProperties.timeout())
                .maxRetries(moderationModelProperties.maxRetries())
                .logRequests(moderationModelProperties.logRequests())
                .logResponses(moderationModelProperties.logResponses())
                .customHeaders(moderationModelProperties.customHeaders())
                .build();
    }

    @Bean(MODERATION_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    @ConditionalOnMissingBean(name = MODERATION_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiModerationModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiModerationModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    OpenAiImageModel openAiImageModel(
            @Qualifier(IMAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        ImageModelProperties imageModelProperties = properties.imageModel();
        return OpenAiImageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(imageModelProperties.baseUrl())
                .apiKey(imageModelProperties.apiKey())
                .organizationId(imageModelProperties.organizationId())
                .projectId(imageModelProperties.projectId())
                .modelName(imageModelProperties.modelName())
                .size(imageModelProperties.size())
                .quality(imageModelProperties.quality())
                .style(imageModelProperties.style())
                .user(imageModelProperties.user())
                .responseFormat(imageModelProperties.responseFormat())
                .timeout(imageModelProperties.timeout())
                .maxRetries(imageModelProperties.maxRetries())
                .logRequests(imageModelProperties.logRequests())
                .logResponses(imageModelProperties.logResponses())
                .customHeaders(imageModelProperties.customHeaders())
                .build();
    }

    @Bean(IMAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    @ConditionalOnMissingBean(name = IMAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiImageModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiImageModel
                .createDefaultStreamingRequestExecutor(false);
    }
}