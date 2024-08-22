package dev.langchain4j.spring.mistralai.client;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.mistralai.internal.api.*;
import dev.langchain4j.model.mistralai.internal.client.MistralAiClient;
import dev.langchain4j.model.mistralai.internal.client.MistralAiClientBuilderFactory;
import dev.langchain4j.spring.core.http.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Client for the Mistral AI API.
 *
 * @see <a href="https://docs.mistral.ai/api/">Mistral AI API</a>
 */
public class SpringMistralAiClient extends MistralAiClient {

    private final Logger logger = LoggerFactory.getLogger(SpringMistralAiClient.class);

    private final RestClient restClient;

    public SpringMistralAiClient(Builder builder) {
        this(MistralAiClientConfig.builder()
                .apiKey(builder.apiKey)
                .baseUrl(URI.create(builder.baseUrl))
                .connectTimeout(builder.timeout)
                .readTimeout(builder.timeout)
                .logRequests(builder.logRequests)
                .logResponses(builder.logResponses)
                .build(), RestClient.builder());
    }

    public SpringMistralAiClient(MistralAiClientConfig clientConfig, RestClient.Builder restClientBuilder) {
        this.restClient = buildRestClient(clientConfig, restClientBuilder);
    }

    private RestClient buildRestClient(MistralAiClientConfig clientConfig, RestClient.Builder restClientBuilder) {
        var clientHttpRequestFactory = new BufferingClientHttpRequestFactory(ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(clientConfig.connectTimeout())
                        .withReadTimeout(clientConfig.readTimeout())));

        Consumer<HttpHeaders> defaultHeaders = headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(clientConfig.apiKey());
        };

        return restClientBuilder.requestFactory(clientHttpRequestFactory)
                .baseUrl(clientConfig.baseUrl().toString())
                .defaultHeaders(defaultHeaders)
                .requestInterceptors(interceptors -> {
                    if (clientConfig.logRequests() || clientConfig.logResponses()) {
                        interceptors.add(new HttpLoggingInterceptor(clientConfig.logRequests(), clientConfig.logResponses()));
                    }
                })
                .build();
    }

    /**
     * Creates a model response for the given chat conversation.
     */
    @Nullable
    @Override
    public MistralAiChatCompletionResponse chatCompletion(MistralAiChatCompletionRequest chatCompletionRequest) {
        Assert.notNull(chatCompletionRequest, "chatCompletionRequest cannot be null");
        Assert.isTrue(!chatCompletionRequest.getStream(), "stream mode must be disabled");

        logger.debug("Sending chat completion request: {}", chatCompletionRequest);

        return this.restClient.post()
                .uri("/chat/completions")
                .body(chatCompletionRequest)
                .retrieve()
                .body(MistralAiChatCompletionResponse.class);
    }

    /**
     * Creates a streaming model response for the given chat conversation.
     */
    @Override
    public void streamingChatCompletion(MistralAiChatCompletionRequest chatCompletionRequest, StreamingResponseHandler<AiMessage> handler) {
        throw new IllegalStateException("operation not implemented");
    }

    /**
     * Creates an embedding vector representing the input text.
     */
    @Nullable
    @Override
    public MistralAiEmbeddingResponse embedding(MistralAiEmbeddingRequest embeddingRequest) {
        Assert.notNull(embeddingRequest, "embeddingRequest cannot be null");

        logger.debug("Sending embedding request: {}", embeddingRequest);

        return this.restClient.post()
                .uri("/embeddings")
                .body(embeddingRequest)
                .retrieve()
                .body(MistralAiEmbeddingResponse.class);
    }

    /**
     * List available models.
     */
    @Nullable
    @Override
    public MistralAiModelResponse listModels() {
        logger.debug("Sending list models request");

        return this.restClient.get()
                .uri("/models")
                .retrieve()
                .body(MistralAiModelResponse.class);
    }

    /**
     * A factory for creating a {@link SpringMistralAiClient.Builder} instance.
     */
    public static class SpringMistralAiClientBuilderFactory implements MistralAiClientBuilderFactory {

        @Override
        public Builder get() {
            return new Builder();
        }

    }

    /**
     * A builder for creating a {@link SpringMistralAiClient} instance.
     */
    public static class Builder extends MistralAiClient.Builder<SpringMistralAiClient, Builder> {

        @Override
        public SpringMistralAiClient build() {
            return new SpringMistralAiClient(this);
        }

    }

}
