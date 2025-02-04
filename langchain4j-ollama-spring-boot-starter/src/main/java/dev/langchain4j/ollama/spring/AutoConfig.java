package dev.langchain4j.ollama.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
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

    private static final String OLLAMA_CHAT_MODEL_HTTP_CLIENT_BUILDER = "ollamaChatModelHttpClientBuilder";

    private static final String OLLAMA_STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER = "ollamaStreamingChatModelHttpClientBuilder";
    private static final String OLLAMA_STREAMING_CHAT_MODEL_TASK_EXECUTOR = "ollamaStreamingChatModelTaskExecutor";

    private static final String OLLAMA_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "ollamaLanguageModelHttpClientBuilder";

    private static final String OLLAMA_STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "ollamaStreamingLanguageModelHttpClientBuilder";
    private static final String OLLAMA_STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR = "ollamaStreamingLanguageModelTaskExecutor";

    private static final String OLLAMA_EMBEDDING_MODEL_HTTP_CLIENT_BUILDER = "ollamaEmbeddingModelHttpClientBuilder";

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.base-url")
    OllamaChatModel ollamaChatModel(
            Properties properties,
            ObjectProvider<ChatModelListener> listeners,
            @Qualifier(OLLAMA_CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return OllamaChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .stop(chatModelProperties.getStop())
                .format(chatModelProperties.getFormat())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .timeout(chatModelProperties.getTimeout())
                .maxRetries(chatModelProperties.getMaxRetries())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean(OLLAMA_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".chat-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaChatModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return new SpringRestClientBuilder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OllamaChatModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    OllamaStreamingChatModel ollamaStreamingChatModel(
            Properties properties,
            ObjectProvider<ChatModelListener> listeners,
            @Qualifier(OLLAMA_STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder
    ) {
        ChatModelProperties chatModelProperties = properties.getStreamingChatModel();
        return OllamaStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.getBaseUrl())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topK(chatModelProperties.getTopK())
                .topP(chatModelProperties.getTopP())
                .repeatPenalty(chatModelProperties.getRepeatPenalty())
                .seed(chatModelProperties.getSeed())
                .numPredict(chatModelProperties.getNumPredict())
                .stop(chatModelProperties.getStop())
                .format(chatModelProperties.getFormat())
                .supportedCapabilities(chatModelProperties.getSupportedCapabilities())
                .timeout(chatModelProperties.getTimeout())
                .customHeaders(chatModelProperties.getCustomHeaders())
                .logRequests(chatModelProperties.getLogRequests())
                .logResponses(chatModelProperties.getLogResponses())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean(OLLAMA_STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaStreamingChatModelHttpClientBuilder(
            ObjectProvider<RestClient.Builder> restClientBuilder,
            @Qualifier(OLLAMA_STREAMING_CHAT_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor) {
        return new SpringRestClientBuilder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(OLLAMA_STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingChatModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(OLLAMA_STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingChatModelTaskExecutorWithoutContextPropagation() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.base-url")
    OllamaLanguageModel ollamaLanguageModel(
            Properties properties,
            @Qualifier(OLLAMA_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder) {
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
                .format(languageModelProperties.getFormat())
                .timeout(languageModelProperties.getTimeout())
                .maxRetries(languageModelProperties.getMaxRetries())
                .customHeaders(languageModelProperties.getCustomHeaders())
                .logRequests(languageModelProperties.getLogRequests())
                .logResponses(languageModelProperties.getLogResponses())
                .build();
    }

    @Bean(OLLAMA_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".language-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaLanguageModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return new SpringRestClientBuilder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OllamaChatModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    OllamaStreamingLanguageModel ollamaStreamingLanguageModel(
            Properties properties,
            @Qualifier(OLLAMA_STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder
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
                .format(languageModelProperties.getFormat())
                .timeout(languageModelProperties.getTimeout())
                .customHeaders(languageModelProperties.getCustomHeaders())
                .logRequests(languageModelProperties.getLogRequests())
                .logResponses(languageModelProperties.getLogResponses())
                .build();
    }

    @Bean(OLLAMA_STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaStreamingLanguageModelHttpClientBuilder(
            ObjectProvider<RestClient.Builder> restClientBuilder,
            @Qualifier(OLLAMA_STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor) {
        return new SpringRestClientBuilder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(OLLAMA_STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingLanguageModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(OLLAMA_STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor ollamaStreamingLanguageModelTaskExecutorWithoutContextPropagation() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.base-url")
    OllamaEmbeddingModel ollamaEmbeddingModel(
            Properties properties,
            @Qualifier(OLLAMA_EMBEDDING_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder) {
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

    @Bean(OLLAMA_EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".embedding-model.base-url")
    @ConditionalOnMissingBean(name = OLLAMA_EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaEmbeddingModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return new SpringRestClientBuilder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OllamaChatModel
                .createDefaultStreamingRequestExecutor(false);
    }
}