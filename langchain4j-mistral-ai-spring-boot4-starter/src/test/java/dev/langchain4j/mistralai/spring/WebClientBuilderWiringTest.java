package dev.langchain4j.mistralai.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientBuilderWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MistralAiAutoConfiguration.class))
            .withPropertyValues(
                        "langchain4j.mistral-ai.chat-model.api-key=test-api-key",
                        "langchain4j.mistral-ai.chat-model.model-name=test-model",
                        "langchain4j.mistral-ai.chat-model.base-url=http://localhost:1",
                        "langchain4j.mistral-ai.embedding-model.api-key=test-api-key",
                        "langchain4j.mistral-ai.embedding-model.model-name=test-model",
                        "langchain4j.mistral-ai.embedding-model.base-url=http://localhost:1",
                        "langchain4j.mistral-ai.fim-model.api-key=test-api-key",
                        "langchain4j.mistral-ai.fim-model.model-name=test-model",
                        "langchain4j.mistral-ai.fim-model.base-url=http://localhost:1",
                        "langchain4j.mistral-ai.moderation-model.api-key=test-api-key",
                        "langchain4j.mistral-ai.moderation-model.model-name=test-model",
                        "langchain4j.mistral-ai.moderation-model.base-url=http://localhost:1",
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=test-model",
                        "langchain4j.mistral-ai.streaming-chat-model.base-url=http://localhost:1",
                        "langchain4j.mistral-ai.streaming-fim-model.api-key=test-api-key",
                        "langchain4j.mistral-ai.streaming-fim-model.model-name=test-model",
                        "langchain4j.mistral-ai.streaming-fim-model.base-url=http://localhost:1"
            );

    @Test
    void should_build_every_model_http_client_from_the_application_web_client_builder() {
        WebClient.Builder applicationWebClientBuilder = WebClient.builder();

        contextRunner
                .withBean(WebClient.Builder.class, () -> applicationWebClientBuilder)
                .run(context -> {

                    Map<String, HttpClientBuilder> httpClientBuilders = context.getBeansOfType(HttpClientBuilder.class);

                    assertThat(httpClientBuilders).hasSize(6);
                    assertThat(httpClientBuilders.values()).allSatisfy(httpClientBuilder ->
                            assertThat(((SpringRestClientBuilder) httpClientBuilder).webClientBuilder().webClientBuilder())
                                    .isSameAs(applicationWebClientBuilder));
                });
    }

    @Test
    void should_use_the_connector_builder_configured_in_spring_boot() {
        ClientHttpConnectorBuilder<?> applicationConnectorBuilder = ClientHttpConnectorBuilder.jdk();

        contextRunner
                .withBean(ClientHttpConnectorBuilder.class, () -> applicationConnectorBuilder)
                .run(context -> assertThat(context.getBeansOfType(HttpClientBuilder.class).values())
                        .hasSize(6)
                        .allSatisfy(httpClientBuilder ->
                                assertThat(((SpringRestClientBuilder) httpClientBuilder).clientHttpConnectorBuilder())
                                        .isSameAs(applicationConnectorBuilder)));
    }

    @Test
    void should_start_when_the_application_has_several_web_client_builders() {
        contextRunner
                .withBean("firstWebClientBuilder", WebClient.Builder.class, WebClient::builder)
                .withBean("secondWebClientBuilder", WebClient.Builder.class, WebClient::builder)
                .run(context -> {

                    assertThat(context).hasNotFailed();
                    assertThat(context.getBeansOfType(HttpClientBuilder.class)).hasSize(6);
                });
    }
}
