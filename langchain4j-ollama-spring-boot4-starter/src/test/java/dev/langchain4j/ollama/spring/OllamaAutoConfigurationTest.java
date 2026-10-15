package dev.langchain4j.ollama.spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.listener.DecisionModelListener;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.ollama.OllamaDecisionModel;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class OllamaAutoConfigurationTest {

    private static final String DECISION_RESPONSE = """
            {"model": "nimble", "answers": {"spam": {"type": "noul", "noul": 0.95}},
             "usage": {"input_tokens": 12, "output_tokens": 0}}
            """;

    private static final DecisionRequest DECISION_REQUEST = DecisionRequest.builder()
            .input("You won a free cruise!")
            .question("spam", YesNoQuestion.of("Is this message spam?"))
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OllamaAutoConfiguration.class));

    @Test
    void should_provide_decision_model() {
        RecordingHttpClientBuilder httpClientBuilder = new RecordingHttpClientBuilder(200, DECISION_RESPONSE);

        contextRunner
                .withBean("ollamaDecisionModelHttpClientBuilder", HttpClientBuilder.class, () -> httpClientBuilder)
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=http://localhost:11434",
                        "langchain4j.ollama.decision-model.model-name=nimble"
                )
                .run(context -> {

                    DecisionModel model = context.getBean(DecisionModel.class);
                    assertThat(model).isInstanceOf(OllamaDecisionModel.class);
                    assertThat(context.getBean(OllamaDecisionModel.class)).isSameAs(model);

                    DecisionResponse response = model.decide(DECISION_REQUEST);
                    assertThat(response.yesNo("spam").probability()).isEqualTo(0.95);

                    HttpRequest request = httpClientBuilder.requests.get(0);
                    assertThat(request.url()).isEqualTo("http://localhost:11434/v1/systemone");
                    assertThat(OBJECT_MAPPER.readTree(request.body()).get("model").asText()).isEqualTo("nimble");
                });
    }

    @Test
    void should_bind_all_decision_model_properties() {
        RecordingHttpClientBuilder httpClientBuilder = new RecordingHttpClientBuilder(200, DECISION_RESPONSE);

        contextRunner
                .withBean("ollamaDecisionModelHttpClientBuilder", HttpClientBuilder.class, () -> httpClientBuilder)
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=http://localhost:11434",
                        "langchain4j.ollama.decision-model.model-name=nimble",
                        "langchain4j.ollama.decision-model.timeout=PT42S",
                        "langchain4j.ollama.decision-model.keep-alive=300",
                        "langchain4j.ollama.decision-model.custom-headers.X-Custom=custom-value",
                        "langchain4j.ollama.decision-model.custom-parameters.custom_parameter=custom-parameter-value",
                        "langchain4j.ollama.decision-model.log-requests=true",
                        "langchain4j.ollama.decision-model.log-responses=true"
                )
                .run(context -> {
                    context.getBean(DecisionModel.class).decide(DECISION_REQUEST);

                    assertThat(httpClientBuilder.connectTimeout).isEqualTo(Duration.ofSeconds(42));
                    assertThat(httpClientBuilder.readTimeout).isEqualTo(Duration.ofSeconds(42));

                    HttpRequest request = httpClientBuilder.requests.get(0);
                    assertThat(request.headers()).containsEntry("X-Custom", List.of("custom-value"));
                    JsonNode body = OBJECT_MAPPER.readTree(request.body());
                    assertThat(body.get("keep_alive").asInt()).isEqualTo(300);
                    assertThat(body.get("custom_parameter").asText()).isEqualTo("custom-parameter-value");
                });
    }

    @Test
    void should_bind_max_retries() {
        RecordingHttpClientBuilder httpClientBuilder = new RecordingHttpClientBuilder(500, "server error");

        contextRunner
                .withBean("ollamaDecisionModelHttpClientBuilder", HttpClientBuilder.class, () -> httpClientBuilder)
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=http://localhost:11434",
                        "langchain4j.ollama.decision-model.model-name=nimble",
                        "langchain4j.ollama.decision-model.max-retries=0"
                )
                .run(context -> {
                    DecisionModel model = context.getBean(DecisionModel.class);

                    assertThatThrownBy(() -> model.decide(DECISION_REQUEST))
                            .isInstanceOf(InternalServerException.class);
                    assertThat(httpClientBuilder.requests).hasSize(1);
                });
    }

    @Test
    void should_provide_decision_model_with_listeners() {
        RecordingHttpClientBuilder httpClientBuilder = new RecordingHttpClientBuilder(200, DECISION_RESPONSE);

        contextRunner
                .withBean("ollamaDecisionModelHttpClientBuilder", HttpClientBuilder.class, () -> httpClientBuilder)
                .withPropertyValues(
                        "langchain4j.ollama.decision-model.base-url=http://localhost:11434",
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

    static class RecordingHttpClientBuilder implements HttpClientBuilder {

        private final int statusCode;
        private final String responseBody;
        private final List<HttpRequest> requests = new CopyOnWriteArrayList<>();
        private Duration connectTimeout;
        private Duration readTimeout;

        RecordingHttpClientBuilder(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        public Duration connectTimeout() {
            return connectTimeout;
        }

        @Override
        public HttpClientBuilder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        @Override
        public Duration readTimeout() {
            return readTimeout;
        }

        @Override
        public HttpClientBuilder readTimeout(Duration timeout) {
            this.readTimeout = timeout;
            return this;
        }

        @Override
        public HttpClient build() {
            return new HttpClient() {

                @Override
                public SuccessfulHttpResponse execute(HttpRequest request) {
                    requests.add(request);
                    if (statusCode != 200) {
                        throw new HttpException(statusCode, responseBody);
                    }
                    return SuccessfulHttpResponse.builder()
                            .statusCode(statusCode)
                            .body(responseBody)
                            .build();
                }

                @Override
                public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
