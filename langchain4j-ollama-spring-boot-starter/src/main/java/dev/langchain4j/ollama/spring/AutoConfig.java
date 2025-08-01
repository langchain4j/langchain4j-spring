package dev.langchain4j.ollama.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.ollama.*;
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

import static dev.langchain4j.ollama.spring.Properties.PREFIX;

@AutoConfiguration(after = RestClientAutoConfiguration.class)
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    private static final String TASK_EXECUTOR_THREAD_NAME_PREFIX = "LangChain4j-Ollama-";

    private static final String CHAT_MODEL_HTTP_CLIENT_BUILDER = "ollamaChatModelHttpClientBuilder";

    private static final String STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER = "ollamaStreamingChatModelHttpClientBuilder";
    private static final String STREAMING_CHAT_MODEL_TASK_EXECUTOR = "ollamaStreamingChatModelTaskExecutor";

    private static final String LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "ollamaLanguageModelHttpClientBuilder";

    private static final String STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "ollamaStreamingLanguageModelHttpClientBuilder";
    private static final String STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR = "ollamaStreamingLanguageModelTaskExecutor";

    private static final String EMBEDDING_MODEL_HTTP_CLIENT_BUILDER = "ollamaEmbeddingModelHttpClientBuilder";

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.base-url")
    OllamaChatModel ollamaChatModel(
            @Qualifier(CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return OllamaChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .mirostat(chatModelProperties.getMirostat())
                .mirostatEta(chatModelProperties.getMirostatEta())
                .mirostatTau(chatModelProperties.getMirostatTau())
                .repeatLastN(chatModelProperties.getRepeatLastN())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .numCtx(chatModelProperties.getNumCtx())
                .stop(chatModelProperties.getStop())
                .minP(chatModelProperties.getMinP())
                .think(chatModelProperties.getThink())
                .returnThinking(chatModelProperties.getReturnThinking())
                .timeout(chatModelProperties.getTimeout())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .maxRetries(chatModelProperties.getMaxRetries())
                .build();
    }

    @Bean(CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".chat-model.base-url")
    @ConditionalOnMissingBean(name = CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaChatModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OllamaChatModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    OllamaStreamingChatModel ollamaStreamingChatModel(
            @Qualifier(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return OllamaStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .mirostat(chatModelProperties.getMirostat())
                .mirostatEta(chatModelProperties.getMirostatEta())
                .mirostatTau(chatModelProperties.getMirostatTau())
                .repeatLastN(chatModelProperties.getRepeatLastN())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .numCtx(chatModelProperties.getNumCtx())
                .stop(chatModelProperties.getStop())
                .minP(chatModelProperties.getMinP())
                .think(chatModelProperties.getThink())
                .returnThinking(chatModelProperties.getReturnThinking())
                .timeout(chatModelProperties.getTimeout())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .build();
    }

    @Bean(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaStreamingChatModelHttpClientBuilder(
            ObjectProvider<RestClient.Builder> restClientBuilder,
            @Qualifier(STREAMING_CHAT_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingChatModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingChatModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.base-url")
    OllamaLanguageModel ollamaLanguageModel(
            @Qualifier(LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        LanguageModelProperties languageModelProperties = properties.getLanguageModel();
        return OllamaLanguageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(languageModelProperties.getBaseUrl())
                .modelName(languageModelProperties.getModelName())
                .temperature(languageModelProperties.getTemperature())
                .topK(languageModelProperties.getTopK())
                .topP(languageModelProperties.getTopP())
                .repeatPenalty(languageModelProperties.getRepeatPenalty())
                .seed(languageModelProperties.getSeed())
                .numPredict(languageModelProperties.getNumPredict())
                .stop(languageModelProperties.getStop())
                .timeout(languageModelProperties.getTimeout())
                .maxRetries(languageModelProperties.getMaxRetries())
                .customHeaders(languageModelProperties.getCustomHeaders())
                .logRequests(languageModelProperties.getLogRequests())
                .logResponses(languageModelProperties.getLogResponses())
                .build();
    }

    @Bean(LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".language-model.base-url")
    @ConditionalOnMissingBean(name = LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaLanguageModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OllamaLanguageModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    OllamaStreamingLanguageModel ollamaStreamingLanguageModel(
            @Qualifier(STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        LanguageModelProperties languageModelProperties = properties.getStreamingLanguageModel();
        return OllamaStreamingLanguageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(languageModelProperties.getBaseUrl())
                .modelName(languageModelProperties.getModelName())
                .temperature(languageModelProperties.getTemperature())
                .topK(languageModelProperties.getTopK())
                .topP(languageModelProperties.getTopP())
                .repeatPenalty(languageModelProperties.getRepeatPenalty())
                .seed(languageModelProperties.getSeed())
                .numPredict(languageModelProperties.getNumPredict())
                .stop(languageModelProperties.getStop())
                .timeout(languageModelProperties.getTimeout())
                .customHeaders(languageModelProperties.getCustomHeaders())
                .logRequests(languageModelProperties.getLogRequests())
                .logResponses(languageModelProperties.getLogResponses())
                .build();
    }

    @Bean(STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaStreamingLanguageModelHttpClientBuilder(
            @Qualifier(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor,
            ObjectProvider<RestClient.Builder> restClientBuilder
    ) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingLanguageModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingLanguageModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.base-url")
    OllamaEmbeddingModel ollamaEmbeddingModel(
            @Qualifier(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return OllamaEmbeddingModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .modelName(embeddingModelProperties.getModelName())
                .timeout(embeddingModelProperties.getTimeout())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .customHeaders(embeddingModelProperties.getCustomHeaders())
                .logRequests(embeddingModelProperties.getLogRequests())
                .logResponses(embeddingModelProperties.getLogResponses())
                .build();
    }

    @Bean(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".embedding-model.base-url")
    @ConditionalOnMissingBean(name = EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaEmbeddingModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OllamaEmbeddingModel
                .createDefaultStreamingRequestExecutor(false);
    }
}