package dev.langchain4j.openai.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.http.client.HttpClientBuilderLoader;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.openai.*;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Verifies that the auto-configuration creates the expected beans and that the properties declared by a user
 * actually reach the wire. The OpenAI API is stubbed, so no API key and no network access are required.
 */
@WireMockTest
class AutoConfigTest {

    private static final String API_KEY = "test-api-key";

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final String COMPLETIONS_PATH = "/v1/completions";
    private static final String EMBEDDINGS_PATH = "/v1/embeddings";
    private static final String MODERATIONS_PATH = "/v1/moderations";
    private static final String IMAGE_GENERATIONS_PATH = "/v1/images/generations";

    private static final String CHAT_COMPLETION_RESPONSE =
            """
            {
              "id": "chatcmpl-1",
              "created": 1700000000,
              "model": "gpt-4o-mini",
              "choices": [
                {
                  "index": 0,
                  "message": {"role": "assistant", "content": "Berlin"},
                  "finish_reason": "stop"
                }
              ],
              "usage": {"prompt_tokens": 10, "completion_tokens": 1, "total_tokens": 11}
            }
            """;

    private static final String COMPLETION_RESPONSE =
            """
            {
              "id": "cmpl-1",
              "created": 1700000000,
              "model": "gpt-3.5-turbo-instruct",
              "choices": [{"index": 0, "text": "Berlin", "finish_reason": "stop"}],
              "usage": {"prompt_tokens": 10, "completion_tokens": 1, "total_tokens": 11}
            }
            """;

    private static final String EMBEDDING_RESPONSE =
            """
            {
              "model": "text-embedding-3-small",
              "data": [{"index": 0, "embedding": [0.1, 0.2, 0.3]}],
              "usage": {"prompt_tokens": 1, "total_tokens": 1}
            }
            """;

    private static final String MODERATION_RESPONSE =
            """
            {
              "id": "modr-1",
              "model": "omni-moderation-latest",
              "results": [{"flagged": true}]
            }
            """;

    private static final String IMAGE_RESPONSE =
            """
            {
              "created": 1700000000,
              "data": [{"b64_json": "aGVsbG8="}]
            }
            """;

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1";
    }

    private static String sseChunks(String... contents) {
        StringBuilder sse = new StringBuilder();
        for (String content : contents) {
            sse.append("data: {\"id\":\"chatcmpl-1\",\"created\":1700000000,\"model\":\"gpt-4o-mini\",\"choices\":")
                    .append("[{\"index\":0,\"delta\":{\"content\":\"")
                    .append(content)
                    .append("\"},\"finish_reason\":null}]}\n\n");
        }
        sse.append("data: {\"id\":\"chatcmpl-1\",\"created\":1700000000,\"model\":\"gpt-4o-mini\",\"choices\":")
                .append("[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n");
        sse.append("data: [DONE]\n\n");
        return sse.toString();
    }

    private static void stubSse(String path, String body, int chunks, int totalDurationMs) {
        WireMock.stubFor(post(urlEqualTo(path)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(body)
                .withChunkedDribbleDelay(chunks, totalDurationMs)));
    }


    @Test
    void should_resolve_spring_rest_client_from_classpath() {
        // this starter excludes langchain4j-http-client-jdk and ships SpringRestClient in its place,
        // so a dependency change that undoes the swap has to fail the build
        assertThat(HttpClientBuilderLoader.loadHttpClientBuilder().build()).isInstanceOf(SpringRestClient.class);
    }

    @Test
    void should_provide_chat_model() {
        WireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.7"
                )
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiChatModel.class);
                    assertThat(context.getBean(OpenAiChatModel.class)).isSameAs(model);

                    assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(CHAT_COMPLETIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-4o-mini")))
                            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("20")))
                            .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.7")))
                            .withRequestBody(matchingJsonPath(
                                    "$.messages[0].content", equalTo("What is the capital of Germany?"))));
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        WireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiChatModel.class);
                    assertThat(context.getBean(OpenAiChatModel.class)).isSameAs(model);

                    assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");

                    ChatModelListener listener1 = context.getBean("listener1", ChatModelListener.class);
                    ChatModelListener listener2 = context.getBean("listener2", ChatModelListener.class);
                    InOrder inOrder = Mockito.inOrder(listener1, listener2);
                    inOrder.verify(listener2).onRequest(any());
                    inOrder.verify(listener1).onRequest(any());
                    inOrder.verify(listener2).onResponse(any());
                    inOrder.verify(listener1).onResponse(any());
                    inOrder.verifyNoMoreInteractions();
                });
    }

    @Test
    void should_provide_streaming_chat_model() throws Exception {
        stubSse(CHAT_COMPLETIONS_PATH, sseChunks("Ber", "lin"), 4, 200);

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiStreamingChatModel.class)).isSameAs(model);

                    ChatResponse chatResponse = chat(model, "What is the capital of Germany?").get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(CHAT_COMPLETIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-4o-mini")))
                            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("20")))
                            .withRequestBody(matchingJsonPath("$.stream", equalTo("true"))));
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_listeners() {
        stubSse(CHAT_COMPLETIONS_PATH, sseChunks("Ber", "lin"), 4, 200);

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiStreamingChatModel.class)).isSameAs(model);

                    ChatResponse chatResponse = chat(model, "What is the capital of Germany?").get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    ChatModelListener listener1 = context.getBean("listener1", ChatModelListener.class);
                    ChatModelListener listener2 = context.getBean("listener2", ChatModelListener.class);
                    InOrder inOrder = Mockito.inOrder(listener1, listener2);
                    inOrder.verify(listener2).onRequest(any());
                    inOrder.verify(listener1).onRequest(any());
                    inOrder.verify(listener2).onResponse(any());
                    inOrder.verify(listener1).onResponse(any());
                    inOrder.verifyNoMoreInteractions();
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_custom_task_executor() {
        stubSse(CHAT_COMPLETIONS_PATH, sseChunks("Ber", "lin"), 4, 200);

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("openAiStreamingChatModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);

                    ChatResponse chatResponse = chat(model, "What is the capital of Germany?").get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_stream_concurrent_requests_without_blocking_each_other() {
        stubSse(CHAT_COMPLETIONS_PATH, sseChunks("Once", " upon", " a", " time"), 6, 1_500);

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=100"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);

                    AtomicReference<LocalDateTime> started1 = new AtomicReference<>();
                    AtomicReference<LocalDateTime> finished1 = new AtomicReference<>();
                    CompletableFuture<ChatResponse> future1 = chat(model, "Tell me a story", started1, finished1);

                    AtomicReference<LocalDateTime> started2 = new AtomicReference<>();
                    AtomicReference<LocalDateTime> finished2 = new AtomicReference<>();
                    CompletableFuture<ChatResponse> future2 = chat(model, "Tell me a story", started2, finished2);

                    assertThat(future1.get(30, SECONDS).aiMessage().text()).isNotBlank();
                    assertThat(future2.get(30, SECONDS).aiMessage().text()).isNotBlank();

                    assertThat(started1.get()).isBefore(finished2.get());
                    assertThat(started2.get()).isBefore(finished1.get());
                });
    }

    @Test
    void should_provide_language_model() {
        WireMock.stubFor(post(urlEqualTo(COMPLETIONS_PATH)).willReturn(okJson(COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.language-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.language-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.language-model.temperature=0.0"
                )
                .run(context -> {

                    LanguageModel model = context.getBean(LanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiLanguageModel.class);
                    assertThat(context.getBean(OpenAiLanguageModel.class)).isSameAs(model);

                    assertThat(model.generate("What is the capital of Germany?").content())
                            .contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(COMPLETIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-3.5-turbo-instruct")))
                            .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.0"))));
                });
    }

    @Test
    void should_provide_streaming_language_model_with_custom_task_executor() {
        stubSse(COMPLETIONS_PATH, streamingCompletionSse(), 3, 200);

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean(
                        "openAiStreamingLanguageModelTaskExecutor",
                        ThreadPoolTaskExecutor.class,
                        () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-language-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-language-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.streaming-language-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingLanguageModel model = context.getBean(StreamingLanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiStreamingLanguageModel.class);

                    CompletableFuture<Response<String>> future = new CompletableFuture<>();
                    model.generate("What is the capital of Germany?", new StreamingResponseHandler<>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<String> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                            future.completeExceptionally(error);
                        }
                    });

                    assertThat(future.get(30, SECONDS).content()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_provide_embedding_model() {
        WireMock.stubFor(post(urlEqualTo(EMBEDDINGS_PATH)).willReturn(okJson(EMBEDDING_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.embedding-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.embedding-model.model-name=text-embedding-3-small"
                )
                .run(context -> {

                    EmbeddingModel model = context.getBean(EmbeddingModel.class);
                    assertThat(model).isInstanceOf(OpenAiEmbeddingModel.class);
                    assertThat(context.getBean(OpenAiEmbeddingModel.class)).isSameAs(model);

                    assertThat(model.embed("hi").content().vector()).containsExactly(0.1f, 0.2f, 0.3f);

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(EMBEDDINGS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("text-embedding-3-small"))));
                });
    }

    @Test
    void should_provide_moderation_model() {
        WireMock.stubFor(post(urlEqualTo(MODERATIONS_PATH)).willReturn(okJson(MODERATION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.moderation-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.moderation-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.moderation-model.model-name=omni-moderation-latest",
                        "langchain4j.open-ai.moderation-model.organization-id=org-123",
                        "langchain4j.open-ai.moderation-model.project-id=proj-456",
                        "langchain4j.open-ai.moderation-model.custom-headers.X-Custom=custom-value"
                )
                .run(context -> {

                    ModerationModel model = context.getBean(ModerationModel.class);
                    assertThat(model).isInstanceOf(OpenAiModerationModel.class);
                    assertThat(context.getBean(OpenAiModerationModel.class)).isSameAs(model);

                    assertThat(model.moderate("He wants to kill them.").content().flagged())
                            .isTrue();

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(MODERATIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withHeader("OpenAI-Organization", equalTo("org-123"))
                            .withHeader("OpenAI-Project", equalTo("proj-456"))
                            .withHeader("X-Custom", equalTo("custom-value"))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("omni-moderation-latest")))
                            .withRequestBody(
                                    matchingJsonPath("$.input[0]", equalTo("He wants to kill them."))));
                });
    }

    @Test
    void should_provide_image_model() {
        WireMock.stubFor(post(urlEqualTo(IMAGE_GENERATIONS_PATH)).willReturn(okJson(IMAGE_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.image-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.image-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.image-model.model-name=gpt-image-1",
                        "langchain4j.open-ai.image-model.quality=low"
                )
                .run(context -> {

                    ImageModel model = context.getBean(ImageModel.class);
                    assertThat(model).isInstanceOf(OpenAiImageModel.class);
                    assertThat(context.getBean(OpenAiImageModel.class)).isSameAs(model);

                    assertThat(model.generate("banana").content().base64Data()).isNotNull();

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(IMAGE_GENERATIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-image-1")))
                            .withRequestBody(matchingJsonPath("$.quality", equalTo("low"))));
                });
    }

    @Test
    void should_bind_custom_parameters_from_properties() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.custom-parameters.key1=value1",
                        "langchain4j.open-ai.chat-model.custom-parameters.key2=value2"
                )
                .run(context -> {
                    OpenAiChatModel model = context.getBean(OpenAiChatModel.class);

                    assertThat(model.defaultRequestParameters().customParameters())
                            .containsEntry("key1", "value1")
                            .containsEntry("key2", "value2");
                });
    }

    private static String streamingCompletionSse() {
        return "data: {\"id\":\"cmpl-1\",\"created\":1700000000,\"model\":\"gpt-3.5-turbo-instruct\","
                + "\"choices\":[{\"index\":0,\"text\":\"Berlin\",\"finish_reason\":null}]}\n\n"
                + "data: {\"id\":\"cmpl-1\",\"created\":1700000000,\"model\":\"gpt-3.5-turbo-instruct\","
                + "\"choices\":[{\"index\":0,\"text\":\"\",\"finish_reason\":\"stop\"}]}\n\n"
                + "data: [DONE]\n\n";
    }

    private static CompletableFuture<ChatResponse> chat(StreamingChatModel model, String userMessage) {
        return chat(model, userMessage, new AtomicReference<>(), new AtomicReference<>());
    }

    private static CompletableFuture<ChatResponse> chat(
            StreamingChatModel model,
            String userMessage,
            AtomicReference<LocalDateTime> started,
            AtomicReference<LocalDateTime> finished) {

        CompletableFuture<ChatResponse> future = new CompletableFuture<>();
        model.chat(userMessage, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                started.compareAndSet(null, LocalDateTime.now());
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                finished.set(LocalDateTime.now());
                future.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    @Test
    void should_configure_proxy_for_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.proxy.type=HTTP",
                        "langchain4j.open-ai.chat-model.proxy.host=proxy.example.com",
                        "langchain4j.open-ai.chat-model.proxy.port=8080")
                .run(context -> assertProxy(context.getBean(
                        "openAiChatModelHttpClientBuilder",
                        SpringRestClientBuilder.class)));
    }

    @Test
    void should_configure_proxy_for_image_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.image-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.image-model.proxy.type=HTTP",
                        "langchain4j.open-ai.image-model.proxy.host=proxy.example.com",
                        "langchain4j.open-ai.image-model.proxy.port=8080")
                .run(context -> assertProxy(context.getBean(
                        "openAiImageModelHttpClientBuilder",
                        SpringRestClientBuilder.class)));
    }

    private static void assertProxy(SpringRestClientBuilder httpClientBuilder) {
        Proxy proxy = httpClientBuilder.proxy();
        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);

        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertThat(address.getHostString()).isEqualTo("proxy.example.com");
        assertThat(address.getPort()).isEqualTo(8080);
    }

    @Configuration
    static class ListenerConfig {

        @Bean
        @Order(2)
        ChatModelListener listener1() {
            return mock(ChatModelListener.class);
        }

        @Bean
        @Order(1)
        ChatModelListener listener2() {
            return mock(ChatModelListener.class);
        }
    }
}
