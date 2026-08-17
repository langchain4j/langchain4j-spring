package dev.langchain4j.openai.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.http.HttpTimeoutException;
import java.util.List;
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
 * Verifies that every property this starter exposes actually reaches the OpenAI API, so that a typo in the
 * auto-configuration cannot go unnoticed. The API is stubbed, so no API key and no network access are required.
 * <p>
 * A few properties cannot be observed on the wire and are covered by their effect instead: {@code timeout} and
 * {@code max-retries} by how the client behaves, {@code max-segments-per-batch} by how many requests are sent,
 * {@code supported-capabilities} and {@code return-thinking} by what the model reports.
 * <p>
 * Two properties are deliberately not asserted: {@code log-requests} and {@code log-responses} only change what
 * is written to the log, so they are merely set here to make sure they bind. Note also that
 * {@code supported-capabilities} and {@code max-retries} exist on the streaming chat model properties but are not
 * supported by {@link dev.langchain4j.model.openai.OpenAiStreamingChatModel}, so they have no effect there.
 * <p>
 * {@code base-url} needs no assertion of its own: nothing below would reach the stub server without it.
 */
@WireMockTest
class OpenAiPropertyBindingTest {

    private static final String API_KEY = "test-api-key";

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";
    private static final String COMPLETIONS_PATH = "/v1/completions";
    private static final String EMBEDDINGS_PATH = "/v1/embeddings";
    private static final String MODERATIONS_PATH = "/v1/moderations";
    private static final String IMAGE_GENERATIONS_PATH = "/v1/images/generations";

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenAiAutoConfiguration.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1";
    }

    // ----------------------------------------------------------------------------------------------- chat model

    private String[] allChatModelProperties(String prefix) {
        return new String[] {
            prefix + ".base-url=" + baseUrl,
            prefix + ".api-key=" + API_KEY,
            prefix + ".organization-id=org-1",
            prefix + ".project-id=proj-1",
            prefix + ".model-name=gpt-4o-mini",
            prefix + ".temperature=0.3",
            prefix + ".top-p=0.4",
            prefix + ".stop=alpha,beta",
            prefix + ".max-tokens=11",
            prefix + ".max-completion-tokens=12",
            prefix + ".presence-penalty=0.5",
            prefix + ".frequency-penalty=0.6",
            prefix + ".logit-bias.50256=-100",
            prefix + ".response-format=json_object",
            prefix + ".seed=42",
            prefix + ".user=user-1",
            prefix + ".parallel-tool-calls=true",
            prefix + ".store=true",
            prefix + ".metadata.key1=value1",
            prefix + ".service-tier=flex",
            prefix + ".reasoning-effort=low",
            prefix + ".timeout=PT30S",
            prefix + ".log-requests=true",
            prefix + ".log-responses=true",
            prefix + ".custom-headers.X-Custom=custom-value",
            prefix + ".custom-query-params.custom-param=param-value",
            prefix + ".custom-parameters.custom-key=custom-value"
        };
    }

    private void assertAllChatModelPropertiesOnTheWire() {
        WireMock.verify(postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                .withQueryParam("custom-param", equalTo("param-value"))
                .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                .withHeader("OpenAI-Organization", equalTo("org-1"))
                .withHeader("OpenAI-Project", equalTo("proj-1"))
                .withHeader("X-Custom", equalTo("custom-value"))
                .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-4o-mini")))
                .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.3")))
                .withRequestBody(matchingJsonPath("$.top_p", equalTo("0.4")))
                .withRequestBody(matchingJsonPath("$.stop[0]", equalTo("alpha")))
                .withRequestBody(matchingJsonPath("$.stop[1]", equalTo("beta")))
                .withRequestBody(matchingJsonPath("$.max_tokens", equalTo("11")))
                .withRequestBody(matchingJsonPath("$.max_completion_tokens", equalTo("12")))
                .withRequestBody(matchingJsonPath("$.presence_penalty", equalTo("0.5")))
                .withRequestBody(matchingJsonPath("$.frequency_penalty", equalTo("0.6")))
                .withRequestBody(matchingJsonPath("$.logit_bias.50256", equalTo("-100")))
                .withRequestBody(matchingJsonPath("$.response_format.type", equalTo("json_object")))
                .withRequestBody(matchingJsonPath("$.seed", equalTo("42")))
                .withRequestBody(matchingJsonPath("$.user", equalTo("user-1")))
                .withRequestBody(matchingJsonPath("$.parallel_tool_calls", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.store", equalTo("true")))
                .withRequestBody(matchingJsonPath("$.metadata.key1", equalTo("value1")))
                .withRequestBody(matchingJsonPath("$.service_tier", equalTo("flex")))
                .withRequestBody(matchingJsonPath("$.reasoning_effort", equalTo("low")))
                .withRequestBody(matchingJsonPath("$.custom-key", equalTo("custom-value"))));
    }

    @Test
    void should_bind_all_chat_model_properties() {
        stubChatCompletion();

        contextRunner
                .withPropertyValues(allChatModelProperties("langchain4j.open-ai.chat-model"))
                .withPropertyValues("langchain4j.open-ai.chat-model.supported-capabilities="
                        + Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model.supportedCapabilities())
                            .containsExactly(Capability.RESPONSE_FORMAT_JSON_SCHEMA);

                    model.chat("hi");

                    assertAllChatModelPropertiesOnTheWire();
                });
    }

    @Test
    void should_bind_all_streaming_chat_model_properties() {
        stubStreamedChatCompletion();

        contextRunner
                .withPropertyValues(allChatModelProperties("langchain4j.open-ai.streaming-chat-model"))
                .run(context -> {

                    chat(context.getBean(StreamingChatModel.class), ChatRequest.builder()
                            .messages(UserMessage.from("hi"))
                            .build());

                    assertAllChatModelPropertiesOnTheWire();
                });
    }

    @Test
    void should_bind_chat_model_strict_tools() {
        stubChatCompletion();

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.strict-tools=true"
                )
                .run(context -> {

                    context.getBean(ChatModel.class).chat(ChatRequest.builder()
                            .messages(UserMessage.from("hi"))
                            .toolSpecifications(ToolSpecification.builder()
                                    .name("getCurrentDate")
                                    .description("get the current date")
                                    .build())
                            .build());

                    WireMock.verify(postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.tools[0].function.strict", equalTo("true"))));
                });
    }

    @Test
    void should_bind_streaming_chat_model_strict_tools() {
        stubStreamedChatCompletion();

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.strict-tools=true"
                )
                .run(context -> {

                    chat(context.getBean(StreamingChatModel.class), ChatRequest.builder()
                            .messages(UserMessage.from("hi"))
                            .toolSpecifications(ToolSpecification.builder()
                                    .name("getCurrentDate")
                                    .description("get the current date")
                                    .build())
                            .build());

                    WireMock.verify(postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                            .withRequestBody(matchingJsonPath("$.tools[0].function.strict", equalTo("true"))));
                });
    }

    @Test
    void should_bind_chat_model_strict_json_schema() {
        stubChatCompletion();

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.strict-json-schema=true"
                )
                .run(context -> {

                    context.getBean(ChatModel.class).chat(jsonSchemaRequest());

                    WireMock.verify(postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                            .withRequestBody(
                                    matchingJsonPath("$.response_format.json_schema.strict", equalTo("true"))));
                });
    }

    @Test
    void should_bind_streaming_chat_model_strict_json_schema() {
        stubStreamedChatCompletion();

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.strict-json-schema=true"
                )
                .run(context -> {

                    chat(context.getBean(StreamingChatModel.class), jsonSchemaRequest());

                    WireMock.verify(postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                            .withRequestBody(
                                    matchingJsonPath("$.response_format.json_schema.strict", equalTo("true"))));
                });
    }

    @Test
    void should_bind_chat_model_return_thinking() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(
                """
                {
                  "id": "chatcmpl-1",
                  "created": 1700000000,
                  "model": "gpt-4o-mini",
                  "choices": [
                    {
                      "index": 0,
                      "message": {"role": "assistant", "content": "Berlin", "reasoning_content": "thinking"},
                      "finish_reason": "stop"
                    }
                  ]
                }
                """)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.return-thinking=true"
                )
                .run(context -> {
                    ChatResponse response = context.getBean(ChatModel.class)
                            .chat(ChatRequest.builder().messages(UserMessage.from("hi")).build());
                    assertThat(response.aiMessage().thinking()).isEqualTo("thinking");
                });

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.return-thinking=false"
                )
                .run(context -> {
                    ChatResponse response = context.getBean(ChatModel.class)
                            .chat(ChatRequest.builder().messages(UserMessage.from("hi")).build());
                    assertThat(response.aiMessage().thinking()).isNull();
                });
    }

    @Test
    void should_bind_chat_model_timeout() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                .willReturn(okJson("{}").withFixedDelay(3_000)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-retries=0",
                        "langchain4j.open-ai.chat-model.timeout=PT0.5S"
                )
                .run(context -> {
                    ChatModel model = context.getBean(ChatModel.class);
                    // this starter does not put Apache HttpComponents on the classpath, so Spring detects the JDK
                    // HTTP client, which reports a read timeout as java.net.http.HttpTimeoutException rather than
                    // as a SocketTimeoutException. SpringRestClient has to map both.
                    assertThatThrownBy(() -> model.chat("hi"))
                            .isInstanceOf(TimeoutException.class)
                            .hasRootCauseInstanceOf(HttpTimeoutException.class);
                });
    }

    @Test
    void should_bind_chat_model_max_retries() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH))
                .willReturn(aResponse().withStatus(500).withBody("server error")));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-retries=2"
                )
                .run(context -> {
                    ChatModel model = context.getBean(ChatModel.class);
                    assertThatThrownBy(() -> model.chat("hi")).isInstanceOf(RuntimeException.class);

                    // the initial attempt plus two retries
                    assertThat(WireMock.findAll(postRequestedFor(urlPathEqualTo(CHAT_COMPLETIONS_PATH))))
                            .hasSize(3);
                });
    }

    // ------------------------------------------------------------------------------------------- language model

    private String[] allLanguageModelProperties(String prefix) {
        return new String[] {
            prefix + ".base-url=" + baseUrl,
            prefix + ".api-key=" + API_KEY,
            prefix + ".organization-id=org-1",
            prefix + ".project-id=proj-1",
            prefix + ".model-name=gpt-3.5-turbo-instruct",
            prefix + ".temperature=0.7",
            prefix + ".timeout=PT30S",
            prefix + ".log-requests=true",
            prefix + ".log-responses=true",
            prefix + ".custom-headers.X-Custom=custom-value",
            prefix + ".custom-query-params.custom-param=param-value"
        };
    }

    private void assertAllLanguageModelPropertiesOnTheWire() {
        WireMock.verify(postRequestedFor(urlPathEqualTo(COMPLETIONS_PATH))
                .withQueryParam("custom-param", equalTo("param-value"))
                .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                .withHeader("OpenAI-Organization", equalTo("org-1"))
                .withHeader("OpenAI-Project", equalTo("proj-1"))
                .withHeader("X-Custom", equalTo("custom-value"))
                .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-3.5-turbo-instruct")))
                .withRequestBody(matchingJsonPath("$.temperature", equalTo("0.7"))));
    }

    @Test
    void should_bind_all_language_model_properties() {
        stubCompletion();

        contextRunner
                .withPropertyValues(allLanguageModelProperties("langchain4j.open-ai.language-model"))
                .run(context -> {
                    context.getBean(LanguageModel.class).generate("hi");
                    assertAllLanguageModelPropertiesOnTheWire();
                });
    }

    @Test
    void should_bind_all_streaming_language_model_properties() {
        WireMock.stubFor(post(urlPathEqualTo(COMPLETIONS_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: {\"id\":\"cmpl-1\",\"created\":1,\"model\":\"m\","
                        + "\"choices\":[{\"index\":0,\"text\":\"ok\",\"finish_reason\":\"stop\"}]}\n\n"
                        + "data: [DONE]\n\n")));

        contextRunner
                .withPropertyValues(allLanguageModelProperties("langchain4j.open-ai.streaming-language-model"))
                .run(context -> {
                    CompletableFuture<Response<String>> future = new CompletableFuture<>();
                    context.getBean(StreamingLanguageModel.class)
                            .generate("hi", new dev.langchain4j.model.StreamingResponseHandler<String>() {

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
                    future.get(30, SECONDS);

                    assertAllLanguageModelPropertiesOnTheWire();
                });
    }

    // ------------------------------------------------------------------------------------------ embedding model

    @Test
    void should_bind_all_embedding_model_properties() {
        stubEmbedding(1);

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.embedding-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.embedding-model.organization-id=org-1",
                        "langchain4j.open-ai.embedding-model.project-id=proj-1",
                        "langchain4j.open-ai.embedding-model.model-name=text-embedding-3-small",
                        "langchain4j.open-ai.embedding-model.dimensions=256",
                        "langchain4j.open-ai.embedding-model.user=user-1",
                        "langchain4j.open-ai.embedding-model.timeout=PT30S",
                        "langchain4j.open-ai.embedding-model.log-requests=true",
                        "langchain4j.open-ai.embedding-model.log-responses=true",
                        "langchain4j.open-ai.embedding-model.custom-headers.X-Custom=custom-value",
                        "langchain4j.open-ai.embedding-model.custom-query-params.custom-param=param-value"
                )
                .run(context -> {
                    context.getBean(EmbeddingModel.class).embed("hi");

                    WireMock.verify(postRequestedFor(urlPathEqualTo(EMBEDDINGS_PATH))
                            .withQueryParam("custom-param", equalTo("param-value"))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withHeader("OpenAI-Organization", equalTo("org-1"))
                            .withHeader("OpenAI-Project", equalTo("proj-1"))
                            .withHeader("X-Custom", equalTo("custom-value"))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("text-embedding-3-small")))
                            .withRequestBody(matchingJsonPath("$.dimensions", equalTo("256")))
                            .withRequestBody(matchingJsonPath("$.user", equalTo("user-1"))));
                });
    }

    @Test
    void should_bind_embedding_model_max_segments_per_batch() {
        stubEmbedding(2);

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.embedding-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.embedding-model.model-name=text-embedding-3-small",
                        "langchain4j.open-ai.embedding-model.max-segments-per-batch=2"
                )
                .run(context -> {
                    context.getBean(EmbeddingModel.class).embedAll(List.of(
                            TextSegment.from("a"), TextSegment.from("b"),
                            TextSegment.from("c"), TextSegment.from("d")));

                    // four segments, two per batch
                    assertThat(WireMock.findAll(postRequestedFor(urlPathEqualTo(EMBEDDINGS_PATH)))).hasSize(2);
                });
    }

    // ----------------------------------------------------------------------------------------- moderation model

    @Test
    void should_bind_all_moderation_model_properties() {
        WireMock.stubFor(post(urlPathEqualTo(MODERATIONS_PATH)).willReturn(okJson(
                """
                {"id": "modr-1", "model": "omni-moderation-latest", "results": [{"flagged": false}]}
                """)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.moderation-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.moderation-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.moderation-model.organization-id=org-1",
                        "langchain4j.open-ai.moderation-model.project-id=proj-1",
                        "langchain4j.open-ai.moderation-model.model-name=omni-moderation-latest",
                        "langchain4j.open-ai.moderation-model.timeout=PT30S",
                        "langchain4j.open-ai.moderation-model.log-requests=true",
                        "langchain4j.open-ai.moderation-model.log-responses=true",
                        "langchain4j.open-ai.moderation-model.custom-headers.X-Custom=custom-value",
                        "langchain4j.open-ai.moderation-model.custom-query-params.custom-param=param-value"
                )
                .run(context -> {
                    context.getBean(ModerationModel.class).moderate("hi");

                    WireMock.verify(postRequestedFor(urlPathEqualTo(MODERATIONS_PATH))
                            .withQueryParam("custom-param", equalTo("param-value"))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withHeader("OpenAI-Organization", equalTo("org-1"))
                            .withHeader("OpenAI-Project", equalTo("proj-1"))
                            .withHeader("X-Custom", equalTo("custom-value"))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("omni-moderation-latest"))));
                });
    }

    // ---------------------------------------------------------------------------------------------- image model

    @Test
    void should_bind_all_image_model_properties() {
        WireMock.stubFor(post(urlPathEqualTo(IMAGE_GENERATIONS_PATH)).willReturn(okJson(
                """
                {"created": 1700000000, "data": [{"b64_json": "aGVsbG8="}]}
                """)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.image-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.image-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.image-model.organization-id=org-1",
                        "langchain4j.open-ai.image-model.project-id=proj-1",
                        "langchain4j.open-ai.image-model.model-name=gpt-image-1",
                        "langchain4j.open-ai.image-model.size=1024x1024",
                        "langchain4j.open-ai.image-model.quality=low",
                        "langchain4j.open-ai.image-model.user=user-1",
                        "langchain4j.open-ai.image-model.background=opaque",
                        "langchain4j.open-ai.image-model.output-format=png",
                        "langchain4j.open-ai.image-model.output-compression=80",
                        "langchain4j.open-ai.image-model.moderation=low",
                        "langchain4j.open-ai.image-model.timeout=PT30S",
                        "langchain4j.open-ai.image-model.log-requests=true",
                        "langchain4j.open-ai.image-model.log-responses=true",
                        "langchain4j.open-ai.image-model.custom-headers.X-Custom=custom-value",
                        "langchain4j.open-ai.image-model.custom-query-params.custom-param=param-value"
                )
                .run(context -> {
                    context.getBean(ImageModel.class).generate("banana");

                    WireMock.verify(postRequestedFor(urlPathEqualTo(IMAGE_GENERATIONS_PATH))
                            .withQueryParam("custom-param", equalTo("param-value"))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withHeader("OpenAI-Organization", equalTo("org-1"))
                            .withHeader("OpenAI-Project", equalTo("proj-1"))
                            .withHeader("X-Custom", equalTo("custom-value"))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-image-1")))
                            .withRequestBody(matchingJsonPath("$.size", equalTo("1024x1024")))
                            .withRequestBody(matchingJsonPath("$.quality", equalTo("low")))
                            .withRequestBody(matchingJsonPath("$.user", equalTo("user-1")))
                            .withRequestBody(matchingJsonPath("$.background", equalTo("opaque")))
                            .withRequestBody(matchingJsonPath("$.output_format", equalTo("png")))
                            .withRequestBody(matchingJsonPath("$.output_compression", equalTo("80")))
                            .withRequestBody(matchingJsonPath("$.moderation", equalTo("low"))));
                });
    }

    @Test
    void should_bind_streaming_chat_model_return_thinking() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: {\"id\":\"c\",\"created\":1,\"model\":\"gpt-4o-mini\",\"choices\":[{\"index\":0,"
                        + "\"delta\":{\"reasoning_content\":\"thinking\"},\"finish_reason\":null}]}\n\n"
                        + "data: {\"id\":\"c\",\"created\":1,\"model\":\"gpt-4o-mini\",\"choices\":[{\"index\":0,"
                        + "\"delta\":{\"content\":\"ok\"},\"finish_reason\":null}]}\n\n"
                        + "data: {\"id\":\"c\",\"created\":1,\"model\":\"gpt-4o-mini\",\"choices\":[{\"index\":0,"
                        + "\"delta\":{},\"finish_reason\":\"stop\"}]}\n\ndata: [DONE]\n\n")));

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.return-thinking=true"
                )
                .run(context -> {
                    ChatResponse response = chatAndGet(context.getBean(StreamingChatModel.class),
                            ChatRequest.builder().messages(UserMessage.from("hi")).build());
                    assertThat(response.aiMessage().thinking()).isEqualTo("thinking");
                });
    }

    @Test
    void should_bind_max_retries_of_the_other_models() {
        WireMock.stubFor(post(WireMock.anyUrl())
                .willReturn(aResponse().withStatus(500).withBody("server error")));

        assertRetries("language-model", COMPLETIONS_PATH,
                context -> context.getBean(LanguageModel.class).generate("hi"));
        assertRetries("embedding-model", EMBEDDINGS_PATH,
                context -> context.getBean(EmbeddingModel.class).embed("hi"));
        assertRetries("moderation-model", MODERATIONS_PATH,
                context -> context.getBean(ModerationModel.class).moderate("hi"));

        // langchain4j.open-ai.image-model.max-retries is deliberately not covered here: OpenAiImageModel calls
        // withRetryMappingExceptions(() -> client.imagesGeneration(request), maxRetries).execute(), so only the
        // building of the request is retried and the HTTP call itself, which is what can fail, is left outside.
        // The property therefore has no effect. That is a bug in langchain4j-open-ai, not in this starter.
    }

    private void assertRetries(String model, String path, java.util.function.Consumer<org.springframework.context.ApplicationContext> call) {
        WireMock.resetAllRequests();
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai." + model + ".base-url=" + baseUrl,
                        "langchain4j.open-ai." + model + ".api-key=" + API_KEY,
                        "langchain4j.open-ai." + model + ".model-name=whatever",
                        "langchain4j.open-ai." + model + ".max-retries=2"
                )
                .run(context -> {
                    assertThatThrownBy(() -> call.accept(context)).isInstanceOf(RuntimeException.class);

                    // the initial attempt plus two retries
                    assertThat(WireMock.findAll(postRequestedFor(urlPathEqualTo(path))))
                            .as("%s should have retried twice", model)
                            .hasSize(3);
                });
    }

    // --------------------------------------------------------------------------------------------------- stubs

    private static void stubChatCompletion() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(
                """
                {
                  "id": "chatcmpl-1",
                  "created": 1700000000,
                  "model": "gpt-4o-mini",
                  "choices": [
                    {"index": 0, "message": {"role": "assistant", "content": "ok"}, "finish_reason": "stop"}
                  ],
                  "usage": {"prompt_tokens": 1, "completion_tokens": 1, "total_tokens": 2}
                }
                """)));
    }

    private static void stubStreamedChatCompletion() {
        WireMock.stubFor(post(urlPathEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: {\"id\":\"chatcmpl-1\",\"created\":1700000000,\"model\":\"gpt-4o-mini\","
                        + "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"ok\"},\"finish_reason\":null}]}\n\n"
                        + "data: {\"id\":\"chatcmpl-1\",\"created\":1700000000,\"model\":\"gpt-4o-mini\","
                        + "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n"
                        + "data: [DONE]\n\n")));
    }

    private static void stubCompletion() {
        WireMock.stubFor(post(urlPathEqualTo(COMPLETIONS_PATH)).willReturn(okJson(
                """
                {
                  "id": "cmpl-1",
                  "created": 1700000000,
                  "model": "gpt-3.5-turbo-instruct",
                  "choices": [{"index": 0, "text": "ok", "finish_reason": "stop"}],
                  "usage": {"prompt_tokens": 1, "completion_tokens": 1, "total_tokens": 2}
                }
                """)));
    }

    private static void stubEmbedding(int embeddingsPerResponse) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < embeddingsPerResponse; i++) {
            if (i > 0) {
                data.append(",");
            }
            data.append("{\"index\":").append(i).append(",\"embedding\":[0.1,0.2,0.3]}");
        }
        WireMock.stubFor(post(urlPathEqualTo(EMBEDDINGS_PATH)).willReturn(okJson(
                "{\"model\":\"text-embedding-3-small\",\"data\":[" + data + "],"
                        + "\"usage\":{\"prompt_tokens\":1,\"total_tokens\":1}}")));
    }

    private static ChatRequest jsonSchemaRequest() {
        return ChatRequest.builder()
                .messages(UserMessage.from("hi"))
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(JsonSchema.builder()
                                .name("Person")
                                .rootElement(JsonObjectSchema.builder().addStringProperty("name").build())
                                .build())
                        .build())
                .build();
    }

    private static ChatResponse chatAndGet(StreamingChatModel model, ChatRequest request) throws Exception {
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

    private static void chat(StreamingChatModel model, ChatRequest request) throws Exception {
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
        future.get(30, SECONDS);
    }
}
