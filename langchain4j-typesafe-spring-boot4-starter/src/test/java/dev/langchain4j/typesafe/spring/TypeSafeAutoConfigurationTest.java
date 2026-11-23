package dev.langchain4j.typesafe.spring;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TypeSafeAutoConfigurationTest {

    private static final String API_KEY = "test-api-key";
    private static final String MODEL_NAME = "jev-latest";
    private static final String SYSTEM_ONE_PATH = "/v1/systemone";

    private static final String SYSTEM_ONE_RESPONSE =
            """
            {
              "model": "jev-latest",
              "answers": {
                "urgent": {
                  "type": "noul",
                  "noul": 0.85
                }
              },
              "usage": {
                "input_tokens": 12,
                "output_tokens": 0
              }
            }
            """;

    private String baseUrl;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TypeSafeAutoConfiguration.class));

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) {
        baseUrl = wireMock.getHttpBaseUrl() + "/";
    }

    @Test
    void should_provide_decision_model_with_api_key() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.typesafe.decision-model.api-key=" + API_KEY,
                        "langchain4j.typesafe.decision-model.model-name=" + MODEL_NAME)
                .run(context -> {
                    assertThat(context).hasSingleBean(DecisionModel.class);
                    assertThat(context).hasSingleBean(TypeSafeDecisionModel.class);
                });
    }

    @Test
    void should_provide_decision_model_with_base_url() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.typesafe.decision-model.base-url=" + baseUrl,
                        "langchain4j.typesafe.decision-model.model-name=" + MODEL_NAME)
                .run(context -> {
                    assertThat(context).hasSingleBean(DecisionModel.class);
                    assertThat(context).hasSingleBean(TypeSafeDecisionModel.class);
                });
    }

    @Test
    void should_not_provide_decision_model_when_no_properties() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(DecisionModel.class);
            assertThat(context).doesNotHaveBean(TypeSafeDecisionModel.class);
        });
    }

    @Test
    void should_execute_decision_against_wiremock() {
        WireMock.stubFor(post(urlEqualTo(SYSTEM_ONE_PATH)).willReturn(okJson(SYSTEM_ONE_RESPONSE)));

        contextRunner
                .withPropertyValues(
                        "langchain4j.typesafe.decision-model.base-url=" + baseUrl,
                        "langchain4j.typesafe.decision-model.model-name=" + MODEL_NAME,
                        "langchain4j.typesafe.decision-model.log-requests=true",
                        "langchain4j.typesafe.decision-model.log-responses=true")
                .run(context -> {
                    DecisionModel model = context.getBean(DecisionModel.class);

                    DecisionRequest request = DecisionRequest.builder()
                            .input("Please help with double charge")
                            .question("urgent", YesNoQuestion.of("Is this request urgent?"))
                            .build();

                    DecisionResponse response = model.decide(request);

                    assertThat(response.modelName()).isEqualTo("jev-latest");
                    YesNoAnswer urgent = response.yesNo("urgent");
                    assertThat(urgent.probability()).isEqualTo(0.85);
                });
    }
}
