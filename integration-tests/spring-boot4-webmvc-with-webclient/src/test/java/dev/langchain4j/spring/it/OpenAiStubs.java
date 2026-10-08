package dev.langchain4j.spring.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Stubs the OpenAI Chat Completions API: a complete response for blocking and non-blocking calls, and a stream of
 * server-sent events for streaming calls. Both answer "Berlin".
 */
class OpenAiStubs {

    static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private static final String CHAT_COMPLETION = chatCompletion("Berlin");

    private static final String CHAT_COMPLETION_CHUNKS = chunk("{\"content\":\"Ber\"}", null)
            + chunk("{\"content\":\"lin\"}", null)
            + chunk("{}", "\"stop\"")
            + "data: [DONE]\n\n";

    static void stub(WireMockExtension wireMock) {
        wireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH))
                .withRequestBody(matchingJsonPath("$.stream", equalTo("true")))
                .atPriority(1)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/event-stream")
                        .withBody(CHAT_COMPLETION_CHUNKS)));
        wireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH))
                .atPriority(2)
                .willReturn(okJson(CHAT_COMPLETION)));
    }

    static String[] properties(WireMockExtension wireMock) {
        String baseUrl = wireMock.baseUrl() + "/v1";
        return new String[] {
                "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                "langchain4j.open-ai.chat-model.api-key=test-api-key",
                "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                "langchain4j.open-ai.streaming-chat-model.base-url=" + baseUrl,
                "langchain4j.open-ai.streaming-chat-model.api-key=test-api-key",
                "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini"
        };
    }

    static String chatCompletion(String content) {
        return """
                {
                  "id": "chatcmpl-1",
                  "created": 1700000000,
                  "model": "gpt-4o-mini",
                  "choices": [{"index": 0, "message": {"role": "assistant", "content": "%s"}, "finish_reason": "stop"}],
                  "usage": {"prompt_tokens": 10, "completion_tokens": 1, "total_tokens": 11}
                }
                """.formatted(content);
    }

    private static String chunk(String delta, String finishReason) {
        return "data: {\"id\":\"chatcmpl-1\",\"created\":1700000000,\"model\":\"gpt-4o-mini\",\"choices\":"
                + "[{\"index\":0,\"delta\":" + delta + ",\"finish_reason\":" + finishReason + "}]}\n\n";
    }
}
