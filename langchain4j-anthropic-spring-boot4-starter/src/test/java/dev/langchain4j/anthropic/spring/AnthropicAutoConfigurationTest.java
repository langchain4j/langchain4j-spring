package dev.langchain4j.anthropic.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the auto-configuration creates the expected beans and that the properties declared by a user
 * actually reach the wire. The Anthropic API is stubbed, so no API key and no network access are required.
 */
@WireMockTest
class AnthropicAutoConfigurationTest {

    private static final String API_KEY = "test-api-key";
    private static final String MODEL_NAME = "claude-haiku-4-5-20251001";
    private static final String MESSAGES_PATH = "/v1/messages";

    private static final String MESSAGE_RESPONSE =
            """
            {
              "id": "msg_1",
              "type": "message",
              "role": "assistant",
              "model": "claude-haiku-4-5-20251001",
              "content": [{"type": "text", "text": "Berlin"}],
              "stop_reason": "end_turn",
              "usage": {"input_tokens": 10, "output_tokens": 1}
            }
            """;

    private static final String MESSAGE_SSE =
            """
            event: message_start
            data: {"type":"message_start","message":{"id":"msg_1","type":"message","role":"assistant",\
            "model":"claude-haiku-4-5-20251001","content":[],"usage":{"input_tokens":10,"output_tokens":0}}}

            event: content_block_start
            data: {"type":"content_block_start","index":0,"content_block":{"type":"text","text":""}}

            event: content_block_delta
            data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Ber"}}

            event: content_block_delta
            data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"lin"}}

            event: content_block_stop
            data: {"type":"content_block_stop","index":0}

            event: message_delta
            data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":2}}

            event: message_stop
            data: {"type":"message_stop"}

            """;

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AnthropicAutoConfiguration.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1";
    }

    @Test
    void should_provide_chat_model() {
        WireMock.stubFor(post(urlEqualTo(MESSAGES_PATH)).willReturn(okJson(MESSAGE_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.chat-model.base-url=" + baseUrl,
                        "langchain4j.anthropic.chat-model.api-key=" + API_KEY,
                        "langchain4j.anthropic.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.anthropic.chat-model.max-tokens=20",
                        "langchain4j.anthropic.chat-model.temperature=0.7",
                        "langchain4j.anthropic.chat-model.log-requests=true",
                        "langchain4j.anthropic.chat-model.log-responses=true"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(AnthropicChatModel.class);
                    assertThat(context.getBean(AnthropicChatModel.class)).isSameAs(chatModel);

                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(MESSAGES_PATH))
                            .withHeader("x-api-key", equalTo(API_KEY))
                            .withHeader("anthropic-version", WireMock.matching(".+"))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("20")))
                            .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.7"))));
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        WireMock.stubFor(post(urlEqualTo(MESSAGES_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(MESSAGE_SSE)
                .withChunkedDribbleDelay(4, 200)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.anthropic.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.anthropic.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.anthropic.streaming-chat-model.max-tokens=20",
                        "langchain4j.anthropic.streaming-chat-model.log-requests=true",
                        "langchain4j.anthropic.streaming-chat-model.log-responses=true"
                )
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(AnthropicStreamingChatModel.class);
                    assertThat(context.getBean(AnthropicStreamingChatModel.class)).isSameAs(streamingChatModel);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    StringBuilder partialResponses = new StringBuilder();
                    streamingChatModel.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                            partialResponses.append(partialResponse);
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

                    ChatResponse response = future.get(30, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Berlin");
                    assertThat(partialResponses.toString()).isEqualTo("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(MESSAGES_PATH))
                            .withHeader("x-api-key", equalTo(API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(MODEL_NAME)))
                            .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("20")))
                            .withRequestBody(matchingJsonPath("$.stream", equalTo("true"))));
                });
    }
}
