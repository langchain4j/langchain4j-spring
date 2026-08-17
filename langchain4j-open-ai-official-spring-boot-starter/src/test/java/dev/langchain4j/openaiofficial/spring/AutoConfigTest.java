package dev.langchain4j.openaiofficial.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialEmbeddingModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialImageModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialStreamingChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

/**
 * Verifies that the auto-configuration creates the expected beans and that the properties declared by a user
 * actually reach the wire. The OpenAI API is stubbed, so no API key and no network access are required.
 * <p>
 * The Microsoft Foundry variant of this starter authenticates differently and is covered by {@link AutoConfigIT}.
 */
@WireMockTest
class AutoConfigTest {

    private static final String API_KEY = "test-api-key";
    private static final String MODEL_NAME = "gpt-5-mini";

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final String IMAGE_GENERATIONS_PATH = "/images/generations";

    private static final String CHAT_COMPLETION_RESPONSE =
            """
            {
              "id": "chatcmpl-1",
              "object": "chat.completion",
              "created": 1700000000,
              "model": "gpt-5-mini",
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
        baseUrl = wireMock.getHttpBaseUrl();
    }

    private static String chatCompletionSse(String... contents) {
        StringBuilder sse = new StringBuilder();
        for (String content : contents) {
            sse.append("data: {\"id\":\"chatcmpl-1\",\"object\":\"chat.completion.chunk\",")
                    .append("\"created\":1700000000,\"model\":\"gpt-5-mini\",")
                    .append("\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"")
                    .append(content)
                    .append("\"},\"finish_reason\":null}]}\n\n");
        }
        sse.append("data: {\"id\":\"chatcmpl-1\",\"object\":\"chat.completion.chunk\",")
                .append("\"created\":1700000000,\"model\":\"gpt-5-mini\",")
                .append("\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n");
        sse.append("data: [DONE]\n\n");
        return sse.toString();
    }

    @Test
    void should_provide_chat_model() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai-official.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai-official.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai-official.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.open-ai-official.chat-model.max-completion-tokens=200"
                )
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiOfficialChatModel.class);
                    assertThat(context.getBean(OpenAiOfficialChatModel.class)).isSameAs(model);

                    assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.max_completion_tokens", equalTo("200"))));
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai-official.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai-official.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai-official.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.open-ai-official.chat-model.max-completion-tokens=200"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiOfficialChatModel.class);

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
    void should_provide_streaming_chat_model() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(chatCompletionSse("Ber", "lin"))
                .withChunkedDribbleDelay(4, 200)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai-official.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai-official.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai-official.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.open-ai-official.streaming-chat-model.max-completion-tokens=200"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    assertThat(model).isInstanceOf(OpenAiOfficialStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiOfficialStreamingChatModel.class)).isSameAs(model);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

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

                    ChatResponse chatResponse = future.get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.stream", equalTo("true"))));
                });
    }

    @Test
    void should_provide_image_model() {
        WireMock.stubFor(post(urlPathEqualTo(IMAGE_GENERATIONS_PATH)).willReturn(okJson(IMAGE_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai-official.image-model.base-url=" + baseUrl,
                        "langchain4j.open-ai-official.image-model.api-key=" + API_KEY,
                        "langchain4j.open-ai-official.image-model.model-name=gpt-image-1",
                        "langchain4j.open-ai-official.image-model.quality=low"
                )
                .run(context -> {

                    ImageModel model = context.getBean(ImageModel.class);
                    assertThat(model).isInstanceOf(OpenAiOfficialImageModel.class);
                    assertThat(context.getBean(OpenAiOfficialImageModel.class)).isSameAs(model);

                    assertThat(model.generate("banana").content().base64Data()).isNotNull();

                    WireMock.verify(WireMock.postRequestedFor(urlPathEqualTo(IMAGE_GENERATIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-image-1")))
                            .withRequestBody(matchingJsonPath("$.quality", equalTo("low"))));
                });
    }

    @Test
    void should_not_create_beans_when_api_key_is_not_set() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(OpenAiOfficialChatModel.class);
            assertThat(context).doesNotHaveBean(OpenAiOfficialStreamingChatModel.class);
            assertThat(context).doesNotHaveBean(OpenAiOfficialEmbeddingModel.class);
            assertThat(context).doesNotHaveBean(OpenAiOfficialImageModel.class);
        });
    }

    @Test
    void should_not_create_chat_model_when_user_provides_own_bean() {
        OpenAiOfficialChatModel customChatModel = mock(OpenAiOfficialChatModel.class);
        contextRunner
                .withBean(OpenAiOfficialChatModel.class, () -> customChatModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.chat-model.api-key=test-key",
                        "langchain4j.open-ai-official.chat-model.model-name=gpt-5-mini"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialChatModel.class);
                    assertThat(context.getBean(OpenAiOfficialChatModel.class)).isSameAs(customChatModel);
                });
    }

    @Test
    void should_not_create_streaming_chat_model_when_user_provides_own_bean() {
        OpenAiOfficialStreamingChatModel customModel = mock(OpenAiOfficialStreamingChatModel.class);
        contextRunner
                .withBean(OpenAiOfficialStreamingChatModel.class, () -> customModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.streaming-chat-model.api-key=test-key",
                        "langchain4j.open-ai-official.streaming-chat-model.model-name=gpt-5-mini"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiOfficialStreamingChatModel.class)).isSameAs(customModel);
                });
    }

    @Test
    void should_not_create_embedding_model_when_user_provides_own_bean() {
        OpenAiOfficialEmbeddingModel customModel = mock(OpenAiOfficialEmbeddingModel.class);
        contextRunner
                .withBean(OpenAiOfficialEmbeddingModel.class, () -> customModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.embedding-model.api-key=test-key",
                        "langchain4j.open-ai-official.embedding-model.model-name=text-embedding-3-small"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialEmbeddingModel.class);
                    assertThat(context.getBean(OpenAiOfficialEmbeddingModel.class)).isSameAs(customModel);
                });
    }

    @Test
    void should_not_create_image_model_when_user_provides_own_bean() {
        OpenAiOfficialImageModel customModel = mock(OpenAiOfficialImageModel.class);
        contextRunner
                .withBean(OpenAiOfficialImageModel.class, () -> customModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.image-model.api-key=test-key",
                        "langchain4j.open-ai-official.image-model.model-name=gpt-image-1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialImageModel.class);
                    assertThat(context.getBean(OpenAiOfficialImageModel.class)).isSameAs(customModel);
                });
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
