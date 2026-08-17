package dev.langchain4j.googleaigemini.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.output.Response;
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
 * actually reach the wire. The Gemini API is stubbed, so no API key and no network access are required.
 */
@WireMockTest
class GoogleAiGeminiAutoConfigurationTest {

    private static final String API_KEY = "test-api-key";
    private static final String MODEL_NAME = "gemini-2.5-flash-lite";

    private static final String EMBEDDING_MODEL_NAME = "gemini-embedding-001";

    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/" + MODEL_NAME + ":generateContent";
    private static final String EMBED_CONTENT_PATH = "/v1beta/models/" + EMBEDDING_MODEL_NAME + ":embedContent";
    private static final String STREAM_GENERATE_CONTENT_PATH =
            "/v1beta/models/" + MODEL_NAME + ":streamGenerateContent?alt=sse";

    private static final String GENERATE_CONTENT_RESPONSE =
            """
            {
              "candidates": [
                {
                  "content": {"role": "model", "parts": [{"text": "Delhi"}]},
                  "finishReason": "STOP",
                  "index": 0
                }
              ],
              "usageMetadata": {"promptTokenCount": 5, "candidatesTokenCount": 1, "totalTokenCount": 6},
              "modelVersion": "gemini-2.5-flash-lite"
            }
            """;

    private static final String EMBED_CONTENT_RESPONSE =
            """
            {
              "embedding": {"values": [0.1, 0.2, 0.3]}
            }
            """;

    private static final String STREAM_GENERATE_CONTENT_SSE =
            """
            data: {"candidates":[{"content":{"role":"model","parts":[{"text":"Del"}]},"index":0}],\
            "modelVersion":"gemini-2.5-flash-lite"}

            data: {"candidates":[{"content":{"role":"model","parts":[{"text":"hi"}]},"finishReason":"STOP",\
            "index":0}],"usageMetadata":{"promptTokenCount":5,"candidatesTokenCount":2,"totalTokenCount":7},\
            "modelVersion":"gemini-2.5-flash-lite"}

            """;

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GoogleAiGeminiAutoConfiguration.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1beta";
    }

    @Test
    void should_provide_chat_model() {
        WireMock.stubFor(post(urlEqualTo(GENERATE_CONTENT_PATH)).willReturn(okJson(GENERATE_CONTENT_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.chat-model.base-url=" + baseUrl,
                        "langchain4j.google-ai-gemini.chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.chat-model.model-name=" + MODEL_NAME
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(GoogleAiGeminiChatModel.class);
                    assertThat(context.getBean(GoogleAiGeminiChatModel.class)).isSameAs(chatModel);

                    assertThat(chatModel.chat("What is the capital of India")).contains("Delhi");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(GENERATE_CONTENT_PATH))
                            .withHeader("x-goog-api-key", equalTo(API_KEY))
                            .withRequestBody(matchingJsonPath(
                                    "$.contents[0].parts[0].text", equalTo("What is the capital of India"))));
                });
    }

    @Test
    void should_provide_chat_model_with_property_values() {
        WireMock.stubFor(post(urlEqualTo(GENERATE_CONTENT_PATH)).willReturn(okJson(GENERATE_CONTENT_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.chat-model.base-url=" + baseUrl,
                        "langchain4j.google-ai-gemini.chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.google-ai-gemini.chat-model.temperature=0.7",
                        "langchain4j.google-ai-gemini.chat-model.max-output-tokens=400"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    ChatRequestParameters defaultParameters = chatModel.defaultRequestParameters();
                    assertThat(defaultParameters.modelName()).isEqualTo(MODEL_NAME);
                    assertThat(defaultParameters.temperature()).isEqualTo(0.7);

                    assertThat(chatModel.chat("What is the capital of India")).contains("Delhi");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(GENERATE_CONTENT_PATH))
                            .withRequestBody(matchingJsonPath("$.generationConfig.temperature", equalTo("0.7")))
                            .withRequestBody(
                                    matchingJsonPath("$.generationConfig.maxOutputTokens", equalTo("400"))));
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        stubSse(STREAM_GENERATE_CONTENT_SSE);

        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.google-ai-gemini.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.streaming-chat-model.model-name=" + MODEL_NAME
                )
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(context.getBean(GoogleAiGeminiStreamingChatModel.class))
                            .isSameAs(streamingChatModel);

                    ChatResponse response =
                            chat(streamingChatModel, "What is the capital of India").get(30, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Delhi");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(STREAM_GENERATE_CONTENT_PATH))
                            .withHeader("x-goog-api-key", equalTo(API_KEY)));
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_property_values() {
        stubSse(STREAM_GENERATE_CONTENT_SSE);

        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.streaming-chat-model.base-url=" + baseUrl,
                        "langchain4j.google-ai-gemini.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.google-ai-gemini.streaming-chat-model.temperature=0.7",
                        "langchain4j.google-ai-gemini.streaming-chat-model.topP=0.9",
                        "langchain4j.google-ai-gemini.streaming-chat-model.topK=40",
                        "langchain4j.google-ai-gemini.streaming-chat-model.max-output-tokens=400",
                        "langchain4j.google-ai-gemini.streaming-chat-model.safety-setting"
                                + ".HARM_CATEGORY_SEXUALLY_EXPLICIT=HARM_BLOCK_THRESHOLD_UNSPECIFIED"
                )
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);

                    ChatResponse response =
                            chat(streamingChatModel, "What is the capital of India").get(30, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Delhi");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(STREAM_GENERATE_CONTENT_PATH))
                            .withRequestBody(matchingJsonPath("$.generationConfig.temperature", equalTo("0.7")))
                            .withRequestBody(matchingJsonPath("$.generationConfig.topP", equalTo("0.9")))
                            .withRequestBody(matchingJsonPath("$.generationConfig.topK", equalTo("40")))
                            .withRequestBody(matchingJsonPath("$.generationConfig.maxOutputTokens", equalTo("400")))
                            .withRequestBody(matchingJsonPath(
                                    "$.safetySettings[0].category", equalTo("HARM_CATEGORY_SEXUALLY_EXPLICIT"))));
                });
    }

    @Test
    void should_provide_embedding_model() {
        WireMock.stubFor(post(urlEqualTo(EMBED_CONTENT_PATH)).willReturn(okJson(EMBED_CONTENT_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.embedding-model.base-url=" + baseUrl,
                        "langchain4j.google-ai-gemini.embedding-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.embedding-model.model-name=" + EMBEDDING_MODEL_NAME
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(GoogleAiEmbeddingModel.class);
                    assertThat(context.getBean(GoogleAiEmbeddingModel.class)).isSameAs(embeddingModel);

                    Response<Embedding> response = embeddingModel.embed("Hi, I live in India");
                    assertThat(response.content().vector()).containsExactly(0.1f, 0.2f, 0.3f);

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(EMBED_CONTENT_PATH))
                            .withHeader("x-goog-api-key", equalTo(API_KEY)));
                });
    }

    @Test
    void should_provide_embedding_model_with_property_values() {
        WireMock.stubFor(post(urlEqualTo(EMBED_CONTENT_PATH)).willReturn(okJson(EMBED_CONTENT_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.embedding-model.base-url=" + baseUrl,
                        "langchain4j.google-ai-gemini.embedding-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.embedding-model.model-name=" + EMBEDDING_MODEL_NAME,
                        "langchain4j.google-ai-gemini.embedding-model.title-metadata-key=title-key",
                        "langchain4j.google-ai-gemini.embedding-model.log-requests-and-responses=true",
                        "langchain4j.google-ai-gemini.embedding-model.max-retries=3",
                        "langchain4j.google-ai-gemini.embedding-model.output-dimensionality=512",
                        "langchain4j.google-ai-gemini.embedding-model.task-type=CLASSIFICATION",
                        "langchain4j.google-ai-gemini.embedding-model.timeout=PT30S"
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(GoogleAiEmbeddingModel.class);
                    assertThat(context.getBean(GoogleAiEmbeddingModel.class)).isSameAs(embeddingModel);

                    embeddingModel.embed("Hi, I live in India");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(EMBED_CONTENT_PATH))
                            .withRequestBody(matchingJsonPath("$.taskType", equalTo("CLASSIFICATION")))
                            .withRequestBody(matchingJsonPath("$.outputDimensionality", equalTo("512"))));
                });
    }

    private static void stubSse(String body) {
        WireMock.stubFor(post(urlEqualTo(STREAM_GENERATE_CONTENT_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(body)
                .withChunkedDribbleDelay(4, 200)));
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
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }
}
