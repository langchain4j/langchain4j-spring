package dev.langchain4j.ollama.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.listener.DecisionModelListener;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.ollama.OllamaDecisionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

@WireMockTest
class OllamaAutoConfigurationTest {

    private static final String SYSTEM_ONE_PATH = "/v1/systemone";

    private static final String DECISION_RESPONSE = """
            {"model": "nimble", "answers": {"spam": {"type": "noul", "noul": 0.95}},
             "usage": {"input_tokens": 12, "output_tokens": 0}}
            """;

    private static final DecisionRequest DECISION_REQUEST = DecisionRequest.builder()
            .input("You won a free cruise!")
            .question("spam", YesNoQuestion.of("Is this message spam?"))
            .build();

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OllamaAutoConfiguration.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl();
    }

    @Test
    void should_provide_decision_model() {
        WireMock.stubFor(post(urlEqualTo(SYSTEM_ONE_PATH)).willReturn(okJson(DECISION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=" + baseUrl,
                        "langchain4j.ollama.decision-model.model-name=nimble"
                )
                .run(context -> {

                    DecisionModel model = context.getBean(DecisionModel.class);
                    assertThat(model).isInstanceOf(OllamaDecisionModel.class);
                    assertThat(context.getBean(OllamaDecisionModel.class)).isSameAs(model);

                    DecisionResponse response = model.decide(DECISION_REQUEST);
                    assertThat(response.yesNo("spam").probability()).isEqualTo(0.95);

                    DecisionResponse asyncResponse = model.decideAsync(DECISION_REQUEST).get(10, SECONDS);
                    assertThat(asyncResponse.yesNo("spam").probability()).isEqualTo(0.95);

                    WireMock.verify(2, postRequestedFor(urlEqualTo(SYSTEM_ONE_PATH))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("nimble")))
                            .withRequestBody(matchingJsonPath("$.questions.spam.type", equalTo("noul"))));
                });
    }

    @Test
    void should_bind_all_decision_model_properties() {
        WireMock.stubFor(post(urlEqualTo(SYSTEM_ONE_PATH)).willReturn(okJson(DECISION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=" + baseUrl,
                        "langchain4j.ollama.decision-model.model-name=nimble",
                        "langchain4j.ollama.decision-model.timeout=PT30S",
                        "langchain4j.ollama.decision-model.max-retries=1",
                        "langchain4j.ollama.decision-model.keep-alive=300",
                        "langchain4j.ollama.decision-model.log-requests=true",
                        "langchain4j.ollama.decision-model.log-responses=true",
                        "langchain4j.ollama.decision-model.custom-headers.X-Custom=custom-value",
                        "langchain4j.ollama.decision-model.custom-parameters.custom-key=custom-value"
                )
                .run(context -> {
                    context.getBean(DecisionModel.class).decide(DECISION_REQUEST);

                    WireMock.verify(postRequestedFor(urlEqualTo(SYSTEM_ONE_PATH))
                            .withHeader("X-Custom", equalTo("custom-value"))
                            .withRequestBody(matchingJsonPath("$.model", equalTo("nimble")))
                            .withRequestBody(matchingJsonPath("$.keep_alive", equalTo("300")))
                            .withRequestBody(matchingJsonPath("$.custom-key", equalTo("custom-value"))));
                });
    }

    @Test
    void should_bind_max_retries() {
        WireMock.stubFor(post(urlEqualTo(SYSTEM_ONE_PATH)).willReturn(aResponse().withStatus(500)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=" + baseUrl,
                        "langchain4j.ollama.decision-model.model-name=nimble",
                        "langchain4j.ollama.decision-model.max-retries=0"
                )
                .run(context -> {
                    DecisionModel model = context.getBean(DecisionModel.class);

                    assertThatThrownBy(() -> model.decide(DECISION_REQUEST))
                            .isInstanceOf(InternalServerException.class);
                    WireMock.verify(1, postRequestedFor(urlEqualTo(SYSTEM_ONE_PATH)));
                });
    }

    @Test
    void should_bind_timeout() {
        WireMock.stubFor(post(urlEqualTo(SYSTEM_ONE_PATH))
                .willReturn(okJson(DECISION_RESPONSE).withFixedDelay(2_000)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=" + baseUrl,
                        "langchain4j.ollama.decision-model.model-name=nimble",
                        "langchain4j.ollama.decision-model.timeout=PT0.2S",
                        "langchain4j.ollama.decision-model.max-retries=0"
                )
                .run(context -> {
                    DecisionModel model = context.getBean(DecisionModel.class);

                    assertThatThrownBy(() -> model.decide(DECISION_REQUEST))
                            .isInstanceOf(TimeoutException.class);
                });
    }

    @Test
    void should_provide_decision_model_with_listeners() {
        WireMock.stubFor(post(urlEqualTo(SYSTEM_ONE_PATH)).willReturn(okJson(DECISION_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=" + baseUrl,
                        "langchain4j.ollama.decision-model.model-name=nimble"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {
                    context.getBean(DecisionModel.class).decide(DECISION_REQUEST);

                    DecisionModelListener listener1 = context.getBean("listener1", DecisionModelListener.class);
                    DecisionModelListener listener2 = context.getBean("listener2", DecisionModelListener.class);
                    InOrder inOrder = inOrder(listener1, listener2);
                    inOrder.verify(listener2).onRequest(any());
                    inOrder.verify(listener1).onRequest(any());
                    inOrder.verify(listener2).onResponse(any());
                    inOrder.verify(listener1).onResponse(any());
                });
    }

    @Test
    void should_not_provide_decision_model_without_base_url() {
        contextRunner
                .withPropertyValues("langchain4j.ollama.decision-model.model-name=nimble")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DecisionModel.class);
                    assertThat(context).doesNotHaveBean("ollamaDecisionModelHttpClientBuilder");
                });
    }

    @Configuration
    static class ListenerConfig {

        @Bean
        @Order(2)
        DecisionModelListener listener1() {
            return mock(DecisionModelListener.class);
        }

        @Bean
        @Order(1)
        DecisionModelListener listener2() {
            return mock(DecisionModelListener.class);
        }
    }
}
