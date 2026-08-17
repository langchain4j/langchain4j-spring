package dev.langchain4j.anthropic.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that every property this starter exposes actually reaches the Anthropic API, so that a typo in the
 * auto-configuration cannot go unnoticed. The API is stubbed, so no API key and no network access are required.
 * <p>
 * A few properties cannot be observed on the wire and are covered by their effect instead: {@code timeout} and
 * {@code max-retries} by how the client behaves, and {@code return-thinking} by what the model reports.
 * <p>
 * Two properties are deliberately not asserted: {@code log-requests} and {@code log-responses} only change what is
 * written to the log, so they are merely set here to make sure they bind. Note also that {@code max-retries} exists
 * on the streaming chat model properties but is not supported by
 * {@link dev.langchain4j.model.anthropic.AnthropicStreamingChatModel}, so it has no effect there.
 * <p>
 * {@code base-url} needs no assertion of its own: nothing below would reach the stub server without it.
 */
@WireMockTest
class AnthropicPropertyBindingTest {

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
              "content": [{"type": "text", "text": "ok"}],
              "stop_reason": "end_turn",
              "usage": {"input_tokens": 10, "output_tokens": 1}
            }
            """;

    private static final String THINKING_RESPONSE =
            """
            {
              "id": "msg_1",
              "type": "message",
              "role": "assistant",
              "model": "claude-haiku-4-5-20251001",
              "content": [
                {"type": "thinking", "thinking": "let me think", "signature": "sig"},
                {"type": "text", "text": "ok"}
              ],
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
            data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"ok"}}

            event: content_block_stop
            data: {"type":"content_block_stop","index":0}

            event: message_delta
            data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":1}}

            event: message_stop
            data: {"type":"message_stop"}

            """;

    private static final String THINKING_SSE =
            """
            event: message_start
            data: {"type":"message_start","message":{"id":"msg_1","type":"message","role":"assistant",\
            "model":"claude-haiku-4-5-20251001","content":[],"usage":{"input_tokens":10,"output_tokens":0}}}

            event: content_block_start
            data: {"type":"content_block_start","index":0,"content_block":{"type":"thinking","thinking":""}}

            event: content_block_delta
            data: {"type":"content_block_delta","index":0,"delta":{"type":"thinking_delta","thinking":"let me think"}}

            event: content_block_delta
            data: {"type":"content_block_delta","index":0,"delta":{"type":"signature_delta","signature":"sig"}}

            event: content_block_stop
            data: {"type":"content_block_stop","index":0}

            event: content_block_start
            data: {"type":"content_block_start","index":1,"content_block":{"type":"text","text":""}}

            event: content_block_delta
            data: {"type":"content_block_delta","index":1,"delta":{"type":"text_delta","text":"ok"}}

            event: content_block_stop
            data: {"type":"content_block_stop","index":1}

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

    private String[] allChatModelProperties(String prefix) {
        return new String[] {
            prefix + ".base-url=" + baseUrl,
            prefix + ".api-key=" + API_KEY,
            prefix + ".version=2023-06-01",
            prefix + ".beta=tools-2024-04-04",
            prefix + ".model-name=" + MODEL_NAME,
            prefix + ".temperature=0.3",
            prefix + ".top-p=0.4",
            prefix + ".top-k=5",
            prefix + ".max-tokens=11",
            prefix + ".stop-sequences=alpha,beta",
            prefix + ".tool-choice=REQUIRED",
            prefix + ".cache-system-messages=true",
            prefix + ".cache-tools=true",
            prefix + ".thinking-type=enabled",
            prefix + ".thinking-budget-tokens=1024",
            prefix + ".send-thinking=true",
            prefix + ".custom-parameters.custom-key=custom-value",
            prefix + ".timeout=PT30S",
            prefix + ".log-requests=true",
            prefix + ".log-responses=true"
        };
    }

    private static void assertAllChatModelPropertiesOnTheWire() {
        WireMock.verify(postRequestedFor(urlPathEqualTo(MESSAGES_PATH))
                .withHeader("x-api-key", equalTo(API_KEY))
                .withHeader("anthropic-version", equalTo("2023-06-01"))
                .withHeader("anthropic-beta", equalTo("tools-2024-04-04"))
                .withRequestBody(matchingJsonPath("$.model", equalTo(MODEL_NAME)))
                .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.3")))
                .withRequestBody(matchingJsonPath("$.top_p", equalTo("0.4")))
                .withRequestBody(matchingJsonPath("$.top_k", equalTo("5")))
                .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("11")))
                .withRequestBody(matchingJsonPath("$.stop_sequences[0]", equalTo("alpha")))
                .withRequestBody(matchingJsonPath("$.stop_sequences[1]", equalTo("beta")))
                .withRequestBody(matchingJsonPath("$.tool_choice.type", equalTo("any")))
                .withRequestBody(matchingJsonPath("$.system[0].cache_control.type", equalTo("ephemeral")))
                .withRequestBody(matchingJsonPath("$.tools[0].cache_control.type", equalTo("ephemeral")))
                .withRequestBody(matchingJsonPath("$.thinking.type", equalTo("enabled")))
                .withRequestBody(matchingJsonPath("$.thinking.budget_tokens", equalTo("1024")))
                .withRequestBody(matchingJsonPath("$.custom-key", equalTo("custom-value"))));
    }

    /**
     * Carries everything the properties above need in order to show up: a system message for
     * {@code cache-system-messages}, a tool for {@code cache-tools} and {@code tool-choice}, and an earlier reply
     * with thinking for {@code send-thinking}.
     */
    private static ChatRequest requestExercisingEveryProperty() {
        return ChatRequest.builder()
                .messages(
                        SystemMessage.from("be brief"),
                        UserMessage.from("hi"),
                        AiMessage.builder()
                                .text("earlier answer")
                                .thinking("earlier thinking")
                                .attributes(Map.of("thinking_signature", "sig"))
                                .build(),
                        UserMessage.from("and now?"))
                .toolSpecifications(ToolSpecification.builder()
                        .name("getCurrentDate")
                        .description("get the current date")
                        .build())
                .build();
    }

    @Test
    void should_bind_all_chat_model_properties() {
        stubMessage();

        contextRunner
                .withPropertyValues(allChatModelProperties("langchain4j.anthropic.chat-model"))
                .run(context -> {
                    context.getBean(ChatModel.class).chat(requestExercisingEveryProperty());

                    assertAllChatModelPropertiesOnTheWire();

                    // send-thinking: the thinking of the earlier reply is sent back
                    assertThat(WireMock.findAll(postRequestedFor(urlPathEqualTo(MESSAGES_PATH)))
                                    .get(0)
                                    .getBodyAsString())
                            .contains("earlier thinking");
                });
    }

    @Test
    void should_bind_all_streaming_chat_model_properties() {
        stubStreamedMessage();

        contextRunner
                .withPropertyValues(allChatModelProperties("langchain4j.anthropic.streaming-chat-model"))
                .run(context -> {
                    chat(context.getBean(StreamingChatModel.class), requestExercisingEveryProperty());

                    assertAllChatModelPropertiesOnTheWire();
                });
    }

    @Test
    void should_bind_chat_model_return_thinking() {
        WireMock.stubFor(post(urlPathEqualTo(MESSAGES_PATH)).willReturn(okJson(THINKING_RESPONSE)));

        for (boolean returnThinking : new boolean[] {true, false}) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.anthropic.chat-model.base-url=" + baseUrl,
                            "langchain4j.anthropic.chat-model.api-key=" + API_KEY,
                            "langchain4j.anthropic.chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.anthropic.chat-model.max-tokens=20",
                            "langchain4j.anthropic.chat-model.return-thinking=" + returnThinking
                    )
                    .run(context -> {
                        ChatResponse response = context.getBean(ChatModel.class)
                                .chat(ChatRequest.builder().messages(UserMessage.from("hi")).build());

                        if (returnThinking) {
                            assertThat(response.aiMessage().thinking()).isEqualTo("let me think");
                        } else {
                            assertThat(response.aiMessage().thinking()).isNull();
                        }
                    });
        }
    }

    @Test
    void should_bind_streaming_chat_model_return_thinking() throws Exception {
        WireMock.stubFor(post(urlPathEqualTo(MESSAGES_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(THINKING_SSE)));

        for (boolean returnThinking : new boolean[] {true, false}) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.anthropic.streaming-chat-model.base-url=" + baseUrl,
                            "langchain4j.anthropic.streaming-chat-model.api-key=" + API_KEY,
                            "langchain4j.anthropic.streaming-chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.anthropic.streaming-chat-model.max-tokens=20",
                            "langchain4j.anthropic.streaming-chat-model.return-thinking=" + returnThinking
                    )
                    .run(context -> {
                        ChatResponse response = chat(context.getBean(StreamingChatModel.class),
                                ChatRequest.builder().messages(UserMessage.from("hi")).build());

                        if (returnThinking) {
                            assertThat(response.aiMessage().thinking()).isEqualTo("let me think");
                        } else {
                            assertThat(response.aiMessage().thinking()).isNull();
                        }
                    });
        }
    }

    @Test
    void should_not_send_thinking_when_send_thinking_is_disabled() {
        stubMessage();

        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.chat-model.base-url=" + baseUrl,
                        "langchain4j.anthropic.chat-model.api-key=" + API_KEY,
                        "langchain4j.anthropic.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.anthropic.chat-model.max-tokens=20",
                        "langchain4j.anthropic.chat-model.send-thinking=false"
                )
                .run(context -> {
                    context.getBean(ChatModel.class).chat(requestExercisingEveryProperty());

                    assertThat(WireMock.findAll(postRequestedFor(urlPathEqualTo(MESSAGES_PATH)))
                                    .get(0)
                                    .getBodyAsString())
                            .doesNotContain("earlier thinking");
                });
    }

    @Test
    void should_bind_chat_model_timeout() {
        WireMock.stubFor(post(urlPathEqualTo(MESSAGES_PATH)).willReturn(okJson("{}").withFixedDelay(3_000)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.chat-model.base-url=" + baseUrl,
                        "langchain4j.anthropic.chat-model.api-key=" + API_KEY,
                        "langchain4j.anthropic.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.anthropic.chat-model.max-tokens=20",
                        "langchain4j.anthropic.chat-model.max-retries=0",
                        "langchain4j.anthropic.chat-model.timeout=PT0.5S"
                )
                .run(context -> {
                    ChatModel model = context.getBean(ChatModel.class);
                    assertThatThrownBy(() -> model.chat("hi")).isInstanceOf(TimeoutException.class);
                });
    }

    @Test
    void should_bind_chat_model_max_retries() {
        WireMock.stubFor(post(urlPathEqualTo(MESSAGES_PATH))
                .willReturn(aResponse().withStatus(500).withBody("server error")));

        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.chat-model.base-url=" + baseUrl,
                        "langchain4j.anthropic.chat-model.api-key=" + API_KEY,
                        "langchain4j.anthropic.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.anthropic.chat-model.max-tokens=20",
                        "langchain4j.anthropic.chat-model.max-retries=2"
                )
                .run(context -> {
                    ChatModel model = context.getBean(ChatModel.class);
                    assertThatThrownBy(() -> model.chat("hi")).isInstanceOf(RuntimeException.class);

                    // the initial attempt plus two retries
                    assertThat(WireMock.findAll(postRequestedFor(urlPathEqualTo(MESSAGES_PATH)))).hasSize(3);
                });
    }

    private static void stubMessage() {
        WireMock.stubFor(post(urlPathEqualTo(MESSAGES_PATH)).willReturn(okJson(MESSAGE_RESPONSE)));
    }

    private static void stubStreamedMessage() {
        WireMock.stubFor(post(urlPathEqualTo(MESSAGES_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(MESSAGE_SSE)));
    }

    private static ChatResponse chat(StreamingChatModel model, ChatRequest request) throws Exception {
        CompletableFuture<ChatResponse> future = new CompletableFuture<>();
        model.chat(request, new StreamingChatResponseHandler() {

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
        return future.get(30, SECONDS);
    }
}
