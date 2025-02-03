package dev.langchain4j.ollama.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.ollama.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import static dev.langchain4j.ollama.spring.Properties.PREFIX;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.boot.autoconfigure.thread.Threading.PLATFORM;
import static org.springframework.boot.autoconfigure.thread.Threading.VIRTUAL;

@AutoConfiguration(after = {RestClientAutoConfiguration.class, TaskExecutionAutoConfiguration.class}) // TODO correct?
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    private static final String OLLAMA_HTTP_CLIENT_BUILDER = "ollamaHttpClientBuilder";
    private static final String OLLAMA_TASK_EXECUTOR = "ollamaTaskExecutor"; // TODO name: add "streaming"?

    @Lazy
    @Bean(OLLAMA_HTTP_CLIENT_BUILDER)
    @Scope(SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(name = OLLAMA_HTTP_CLIENT_BUILDER)
    HttpClientBuilder ollamaHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return new SpringRestClientBuilder().restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder));
    }

    @Lazy
    @Bean(OLLAMA_TASK_EXECUTOR)
    @ConditionalOnThreading(VIRTUAL)
    @ConditionalOnMissingBean(name = OLLAMA_TASK_EXECUTOR)
    SimpleAsyncTaskExecutor ollamaTaskExecutorVirtualThreads(ObjectProvider<SimpleAsyncTaskExecutorBuilder> builder) {
        return builder.getIfAvailable(SimpleAsyncTaskExecutorBuilder::new)
                .threadNamePrefix("LangChain4j-Ollama-")
                .build();
    }

    @Lazy
    @Bean(OLLAMA_TASK_EXECUTOR)
    @ConditionalOnThreading(PLATFORM)
    @ConditionalOnMissingBean(name = OLLAMA_TASK_EXECUTOR)
    ThreadPoolTaskExecutor ollamaTaskExecutor(ObjectProvider<ThreadPoolTaskExecutorBuilder> builder) {
        return builder.getIfAvailable(ThreadPoolTaskExecutorBuilder::new)
                .threadNamePrefix("LangChain4j-Ollama-")
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.base-url")
    OllamaChatModel ollamaChatModel(
            @Qualifier(OLLAMA_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        if (httpClientBuilder instanceof SpringRestClientBuilder springRestClientBuilder) {
            springRestClientBuilder.createDefaultStreamingRequestExecutor(false);
        }
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

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.base-url")
    OllamaStreamingChatModel ollamaStreamingChatModel(
            @Qualifier(OLLAMA_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            @Qualifier(OLLAMA_TASK_EXECUTOR) AsyncTaskExecutor streamingRequestExecutor,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        if (httpClientBuilder instanceof SpringRestClientBuilder springRestClientBuilder) {
            springRestClientBuilder.streamingRequestExecutor(streamingRequestExecutor);
        }
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

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.base-url")
    OllamaLanguageModel ollamaLanguageModel(
            @Qualifier(OLLAMA_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        if (httpClientBuilder instanceof SpringRestClientBuilder springRestClientBuilder) {
            springRestClientBuilder.createDefaultStreamingRequestExecutor(false);
        }
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

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.base-url")
    OllamaStreamingLanguageModel ollamaStreamingLanguageModel(
            @Qualifier(OLLAMA_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            @Qualifier(OLLAMA_TASK_EXECUTOR) AsyncTaskExecutor streamingRequestExecutor,
            Properties properties
    ) {
        if (httpClientBuilder instanceof SpringRestClientBuilder springRestClientBuilder) {
            springRestClientBuilder.streamingRequestExecutor(streamingRequestExecutor);
        }
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

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.base-url")
    OllamaEmbeddingModel ollamaEmbeddingModel(
            @Qualifier(OLLAMA_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        if (httpClientBuilder instanceof SpringRestClientBuilder springRestClientBuilder) {
            springRestClientBuilder.createDefaultStreamingRequestExecutor(false);
        }
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
}