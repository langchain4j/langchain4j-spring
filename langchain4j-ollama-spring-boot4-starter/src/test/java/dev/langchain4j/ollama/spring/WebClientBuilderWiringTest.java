package dev.langchain4j.ollama.spring;

import dev.langchain4j.http.client.spring.restclient.WebClientBuilderHolder;
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
            .withConfiguration(AutoConfigurations.of(OllamaAutoConfiguration.class))
            .withPropertyValues(
                        "langchain4j.ollama.chat-model.base-url=http://localhost:1",
                        "langchain4j.ollama.chat-model.model-name=test-model",
                        "langchain4j.ollama.embedding-model.base-url=http://localhost:1",
                        "langchain4j.ollama.embedding-model.model-name=test-model",
                        "langchain4j.ollama.language-model.base-url=http://localhost:1",
                        "langchain4j.ollama.language-model.model-name=test-model",
                        "langchain4j.ollama.streaming-chat-model.base-url=http://localhost:1",
                        "langchain4j.ollama.streaming-chat-model.model-name=test-model",
                        "langchain4j.ollama.streaming-language-model.base-url=http://localhost:1",
                        "langchain4j.ollama.streaming-language-model.model-name=test-model"
            );

    @Test
    void should_build_every_model_http_client_from_the_application_web_client_builder() {
        WebClient.Builder applicationWebClientBuilder = WebClient.builder();

        contextRunner
                .withBean(WebClient.Builder.class, () -> applicationWebClientBuilder)
                .run(context -> {

                    Map<String, HttpClientBuilder> httpClientBuilders = context.getBeansOfType(HttpClientBuilder.class);

                    assertThat(httpClientBuilders).hasSize(5);
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
                        .hasSize(5)
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
                    assertThat(context.getBeansOfType(HttpClientBuilder.class)).hasSize(5);
                    // the application's builders are ambiguous, so neither of them is used
                    assertThat(context.getBean(WebClientBuilderHolder.class).webClientBuilder())
                            .isNotSameAs(context.getBean("firstWebClientBuilder"))
                            .isNotSameAs(context.getBean("secondWebClientBuilder"));
                });
    }
}
