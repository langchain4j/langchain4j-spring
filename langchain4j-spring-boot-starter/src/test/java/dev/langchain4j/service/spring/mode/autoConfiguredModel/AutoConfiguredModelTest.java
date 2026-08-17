package dev.langchain4j.service.spring.mode.autoConfiguredModel;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The other AI service tests supply the model directly as a {@code ChatModelMock} bean, because what they are
 * about is how Spring assembles an AI service, not how a model talks to a provider. That leaves one seam
 * uncovered: an AI service also has to work when the model bean comes from a starter's auto-configuration
 * rather than from a {@code @Bean} method. This test covers exactly that, with the OpenAI API stubbed.
 */
@WireMockTest
class AutoConfiguredModelTest {

    private static final String API_KEY = "test-api-key";
    private static final String MODEL_NAME = "gpt-4o-mini";
    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

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

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/v1";
        WireMock.stubFor(post(urlEqualTo(CHAT_COMPLETIONS_PATH)).willReturn(okJson(CHAT_COMPLETION_RESPONSE)));
    }

    @Test
    void should_create_AI_service_with_auto_configured_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + baseUrl,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(AutoConfiguredModelApplication.class)
                .run(context -> {

                    // given the model bean was created by the OpenAI starter, not declared by the application
                    assertThat(context).hasSingleBean(OpenAiChatModel.class);
                    AutoConfiguredModelAiService aiService = context.getBean(AutoConfiguredModelAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then the auto-configured model was the one the AI service used
                    assertThat(answer).containsIgnoringCase("Berlin");

                    WireMock.verify(WireMock.postRequestedFor(urlEqualTo(CHAT_COMPLETIONS_PATH))
                            .withHeader("Authorization", equalTo("Bearer " + API_KEY))
                            .withRequestBody(matchingJsonPath("$.model", equalTo(MODEL_NAME)))
                            .withRequestBody(matchingJsonPath(
                                    "$.messages[0].content", equalTo("What is the capital of Germany?"))));
                });
    }
}
