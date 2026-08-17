package dev.langchain4j.mistralai.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.http.client.HttpClientBuilderLoader;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiFimModel;
import dev.langchain4j.model.mistralai.MistralAiModerationModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingFimModel;
import dev.langchain4j.model.moderation.ModerationModel;
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

import java.util.concurrent.CompletableFuture;

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
 * actually reach the wire. The Mistral AI API is stubbed, so no API key and no network access are required.
 */
@WireMockTest
class AutoConfigTest {

    private static final String API_KEY = "test-api-key";
    private static final String CHAT_MODEL_NAME = "ministral-3b-latest";
    private static final String FIM_MODEL_NAME = "codestral-2508";
    private static final String EMBEDDING_MODEL_NAME = "mistral-embed";
    private static final String MODERATION_MODEL_NAME = "mistral-moderation-latest";

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final String FIM_COMPLETIONS_PATH = "/v1/fim/completions";
    private static final String EMBEDDINGS_PATH = "/v1/embeddings";
    private static final String MODERATIONS_PATH = "/v1/moderations";

    private static final String CHAT_COMPLETION_RESPONSE =
            """
            {
              "id": "cmpl-1",
              "created": 1700000000,
              "model": "ministral-3b-latest",
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

    private static final String EMBEDDING_RESPONSE =
            """
            {
              "id": "emb-1",
              "object": "list",
              "model": "mistral-embed",
              "data": [{"object": "embedding", "index": 0, "embedding": [0.1, 0.2, 0.3]}],
              "usage": {"prompt_tokens": 1, "total_tokens": 1}
            }
            """;

    private static final String MODERATION_RESPONSE =
            """
            {
              "id": "mod-1",
              "model": "mistral-moderation-latest",
              "results": [{"categories": {"violence_and_threats": false}, "category_scores": {"violence_and_threats": 0.01}}]
            }
            """;

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1";
    }

    private static String chatCompletionSse(String... contents) {
        StringBuilder sse = new StringBuilder();
        for (String content : contents) {
            sse.append("data: {\"id\":\"cmpl-1\",\"created\":1700000000,\"model\":\"ministral-3b-latest\",")
                    .append("\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"")
                    .append(content)
                    .append("\"},\"finish_reason\":null}]}\n\n");
        }
        sse.append("data: {\"id\":\"cmpl-1\",\"created\":1700000000,\"model\":\"ministral-3b-latest\",")
                .append("\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],")
                .append("\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":2,\"total_tokens\":12}}\n\n");
        sse.append("data: [DONE]\n\n");
        return sse.toString();
    }

    private static void stubSse(String path, String body) {
        WireMock.stubFor(post(urlEqualTo(path)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(body)
                .withChunkedDribbleDelay(4, 200)));
    }


    @Test
    void should_resolve_spring_rest_client_from_classpath() {
        // this starter excludes langchain4j-http-client-jdk and ships SpringRestClient in its place,
        // so a dependency change that undoes the swap has to fail the build
        assertThat(HttpClientBuilderLoader.loadHttpClientBuilder().build()).isInstanceOf(SpringRestClient.class);
    }

    // DELIBERATE ERROR (ci-reporting-verification): must be counted as an error, not a failure, and its message
    // must be folded into a <details> block because it is longer than 500 characters
    @Test
    void deliberately_throws_a_long_message() {
        throw new IllegalStateException("ci-reporting-verification: " + "long message ".repeat(60));
    }

    @Test
    void should_provide_chat_model() {
        WireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.chat-model.model-name=" + CHAT_MODEL_NAME,
                        "langchain4j.mistral-ai.chat-model.max-tokens=20",
                        "langchain4j.mistral-ai.chat-model.temperature=0.7"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(MistralAiChatModel.class);
                    assertThat(context.getBean(MistralAiChatModel.class)).isSameAs(chatModel);

                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(CHAT_COMPLETIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(CHAT_MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("20")))
                            .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.7"))));
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        WireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.chat-model.model-name=" + CHAT_MODEL_NAME,
                        "langchain4j.mistral-ai.chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");

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
    void should_provide_streaming_chat_model() {
        stubSse(CHAT_COMPLETIONS_PATH, chatCompletionSse("Ber", "lin"));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=" + CHAT_MODEL_NAME,
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    assertThat(model).isInstanceOf(MistralAiStreamingChatModel.class);
                    assertThat(context.getBean(MistralAiStreamingChatModel.class)).isSameAs(model);

                    ChatResponse chatResponse = chat(model, "What is the capital of Germany?").get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(CHAT_COMPLETIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(CHAT_MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.stream", equalTo("true"))));
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_listeners() {
        stubSse(CHAT_COMPLETIONS_PATH, chatCompletionSse("Ber", "lin"));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=" + CHAT_MODEL_NAME,
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
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
        stubSse(CHAT_COMPLETIONS_PATH, chatCompletionSse("Ber", "lin"));

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("mistralAiStreamingChatModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=" + CHAT_MODEL_NAME,
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    ChatResponse chatResponse = chat(model, "What is the capital of Germany?").get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_provide_embedding_model() {
        WireMock.stubFor(post(urlEqualTo(EMBEDDINGS_PATH)).willReturn(okJson(EMBEDDING_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.embedding-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.embedding-model.model-name=" + EMBEDDING_MODEL_NAME
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(MistralAiEmbeddingModel.class);
                    assertThat(context.getBean(MistralAiEmbeddingModel.class)).isSameAs(embeddingModel);

                    assertThat(embeddingModel.embed("hello").content().vector())
                            .containsExactly(0.1f, 0.2f, 0.3f);

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(EMBEDDINGS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(EMBEDDING_MODEL_NAME))));
                });
    }

    @Test
    void should_provide_fim_model() {
        WireMock.stubFor(post(urlEqualTo(FIM_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.fim-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.fim-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.fim-model.model-name=" + FIM_MODEL_NAME,
                        "langchain4j.mistral-ai.fim-model.max-tokens=20"
                )
                .run(context -> {

                    LanguageModel fimModel = context.getBean(LanguageModel.class);
                    assertThat(fimModel).isInstanceOf(MistralAiFimModel.class);
                    assertThat(context.getBean(MistralAiFimModel.class)).isSameAs(fimModel);

                    assertThat(fimModel.generate("public static void main(").content()).isNotBlank();

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(FIM_COMPLETIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(FIM_MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("20")))
                            .withRequestBody(matchingJsonPath("$.prompt", equalTo("public static void main("))));
                });
    }

    @Test
    void should_provide_streaming_fim_model() {
        stubSse(FIM_COMPLETIONS_PATH, chatCompletionSse("Ber", "lin"));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-fim-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.streaming-fim-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-fim-model.model-name=" + FIM_MODEL_NAME,
                        "langchain4j.mistral-ai.streaming-fim-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingLanguageModel model = context.getBean(StreamingLanguageModel.class);
                    assertThat(model).isInstanceOf(MistralAiStreamingFimModel.class);
                    assertThat(context.getBean(MistralAiStreamingFimModel.class)).isSameAs(model);

                    assertThat(generate(model, "public static void main(").get(30, SECONDS).content())
                            .contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(FIM_COMPLETIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(FIM_MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.stream", equalTo("true"))));
                });
    }

    @Test
    void should_provide_streaming_fim_model_with_custom_task_executor() {
        stubSse(FIM_COMPLETIONS_PATH, chatCompletionSse("Ber", "lin"));

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("mistralAiStreamingFimModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-fim-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.streaming-fim-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-fim-model.model-name=" + FIM_MODEL_NAME,
                        "langchain4j.mistral-ai.streaming-fim-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingLanguageModel model = context.getBean(StreamingLanguageModel.class);
                    assertThat(generate(model, "public static void main(").get(30, SECONDS).content())
                            .contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_provide_moderation_model() {
        WireMock.stubFor(post(urlEqualTo(MODERATIONS_PATH)).willReturn(okJson(MODERATION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.moderation-model.base-url=" + baseUrl,
                        "langchain4j.mistral-ai.moderation-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.moderation-model.model-name=" + MODERATION_MODEL_NAME
                )
                .run(context -> {

                    ModerationModel moderationModel = context.getBean(ModerationModel.class);
                    assertThat(moderationModel).isInstanceOf(MistralAiModerationModel.class);
                    assertThat(context.getBean(MistralAiModerationModel.class)).isSameAs(moderationModel);

                    assertThat(moderationModel.moderate("I want to hug them.").content().flagged())
                            .isFalse();

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(MODERATIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(MODERATION_MODEL_NAME))));
                });
    }

    private static CompletableFuture<ChatResponse> chat(StreamingChatModel model, String userMessage) {
        CompletableFuture<ChatResponse> future = new CompletableFuture<>();
        model.chat(userMessage, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                future.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    private static CompletableFuture<Response<String>> generate(StreamingLanguageModel model, String prompt) {
        CompletableFuture<Response<String>> future = new CompletableFuture<>();
        model.generate(prompt, new StreamingResponseHandler<>() {

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
        return future;
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
