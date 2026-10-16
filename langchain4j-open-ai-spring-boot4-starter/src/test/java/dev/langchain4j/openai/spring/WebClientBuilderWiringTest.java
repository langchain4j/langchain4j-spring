package dev.langchain4j.openai.spring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilders;
import org.slf4j.LoggerFactory;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.imperative.ImperativeHttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.reactive.ReactiveHttpClientAutoConfiguration;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.http.client.HttpRequest;

import java.util.Map;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static dev.langchain4j.http.client.HttpMethod.POST;
import static java.util.concurrent.TimeUnit.SECONDS;

class WebClientBuilderWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenAiAutoConfiguration.class))
            .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.chat-model.model-name=test-model",
                        "langchain4j.open-ai.chat-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.decision-model.api-key=test-api-key",
                        "langchain4j.open-ai.decision-model.model-name=test-model",
                        "langchain4j.open-ai.decision-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.embedding-model.api-key=test-api-key",
                        "langchain4j.open-ai.embedding-model.model-name=test-model",
                        "langchain4j.open-ai.embedding-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.image-model.api-key=test-api-key",
                        "langchain4j.open-ai.image-model.model-name=test-model",
                        "langchain4j.open-ai.image-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.language-model.api-key=test-api-key",
                        "langchain4j.open-ai.language-model.model-name=test-model",
                        "langchain4j.open-ai.language-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.moderation-model.api-key=test-api-key",
                        "langchain4j.open-ai.moderation-model.model-name=test-model",
                        "langchain4j.open-ai.moderation-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.streaming-chat-model.model-name=test-model",
                        "langchain4j.open-ai.streaming-chat-model.base-url=http://localhost:1",
                        "langchain4j.open-ai.streaming-language-model.api-key=test-api-key",
                        "langchain4j.open-ai.streaming-language-model.model-name=test-model",
                        "langchain4j.open-ai.streaming-language-model.base-url=http://localhost:1"
            );

    @Test
    void should_build_every_model_http_client_from_the_application_web_client_builder() {
        WebClient.Builder applicationWebClientBuilder = WebClient.builder();

        contextRunner
                .withBean(WebClient.Builder.class, () -> applicationWebClientBuilder)
                .run(context -> {

                    Map<String, HttpClientBuilder> httpClientBuilders = context.getBeansOfType(HttpClientBuilder.class);

                    assertThat(httpClientBuilders).hasSize(8);
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
                        .hasSize(8)
                        .allSatisfy(httpClientBuilder ->
                                assertThat(((SpringRestClientBuilder) httpClientBuilder).clientHttpConnectorBuilder())
                                        .isSameAs(applicationConnectorBuilder)));
    }

    @Test
    void should_build_every_model_http_client_with_the_http_client_settings_configured_in_spring_boot() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(
                        HttpClientAutoConfiguration.class,
                        ImperativeHttpClientAutoConfiguration.class,
                        ReactiveHttpClientAutoConfiguration.class))
                .withPropertyValues("spring.http.clients.redirects=dont-follow")
                .run(context -> assertThat(context.getBeansOfType(HttpClientBuilder.class).values())
                        .hasSize(8)
                        .allSatisfy(httpClientBuilder -> {
                            SpringRestClientBuilder springRestClientBuilder = (SpringRestClientBuilder) httpClientBuilder;
                            assertThat(springRestClientBuilder.clientHttpRequestFactoryBuilder())
                                    .isSameAs(context.getBean(ClientHttpRequestFactoryBuilder.class));
                            assertThat(springRestClientBuilder.httpClientSettings())
                                    .isSameAs(context.getBean(HttpClientSettings.class))
                                    .extracting(HttpClientSettings::redirects)
                                    .isEqualTo(HttpRedirects.DONT_FOLLOW);
                        }));
    }

    @Test
    void should_send_plain_http_requests_over_http_1_1_with_the_jdk_client_configured_in_spring_boot() throws Exception {
        CompletableFuture<Headers> requestHeaders = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            requestHeaders.complete(exchange.getRequestHeaders());
            exchange.getRequestBody().readAllBytes();
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        try {
            contextRunner
                    .withConfiguration(AutoConfigurations.of(
                            HttpClientAutoConfiguration.class,
                            ImperativeHttpClientAutoConfiguration.class))
                    .withPropertyValues("spring.http.clients.imperative.factory=jdk")
                    .run(context -> {
                        HttpClientBuilder httpClientBuilder =
                                context.getBean("openAiChatModelHttpClientBuilder", HttpClientBuilder.class);

                        httpClientBuilder.build().execute(HttpRequest.builder()
                                .method(POST)
                                .url("http://localhost:" + server.getAddress().getPort() + "/")
                                .body("{}")
                                .build());

                        // HTTP/2 over plain HTTP would send an "Upgrade: h2c" request, which many servers mishandle
                        assertThat(requestHeaders.get(5, SECONDS).containsKey("Upgrade")).isFalse();
                    });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void should_warn_and_use_none_of_several_http_client_settings_when_none_of_them_is_primary() {
        Logger logger = (Logger) LoggerFactory.getLogger(SpringRestClientBuilders.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            contextRunner
                    .withBean("firstSettings", HttpClientSettings.class, HttpClientSettings::defaults)
                    .withBean("secondSettings", HttpClientSettings.class, HttpClientSettings::defaults)
                    .run(context -> {

                        assertThat(context.getBeansOfType(HttpClientBuilder.class).values()).allSatisfy(httpClientBuilder ->
                                assertThat(((SpringRestClientBuilder) httpClientBuilder).httpClientSettings()).isNull());

                        assertThat(appender.list)
                                .filteredOn(event -> event.getLevel() == Level.WARN)
                                .extracting(ILoggingEvent::getFormattedMessage)
                                .isNotEmpty()
                                .allSatisfy(message -> assertThat(message)
                                        .contains(HttpClientSettings.class.getName())
                                        .contains("firstSettings, secondSettings"));
                    });
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void should_warn_and_use_none_of_several_builders_when_none_of_them_is_primary() {
        Logger logger = (Logger) LoggerFactory.getLogger(SpringRestClientBuilders.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            contextRunner
                    .withBean("firstRestClientBuilder", RestClient.Builder.class, RestClient::builder)
                    .withBean("secondRestClientBuilder", RestClient.Builder.class, RestClient::builder)
                    .withBean("firstWebClientBuilder", WebClient.Builder.class, WebClient::builder)
                    .withBean("secondWebClientBuilder", WebClient.Builder.class, WebClient::builder)
                    .run(context -> {

                        assertThat(context).hasNotFailed();
                        assertThat(context.getBeansOfType(HttpClientBuilder.class)).hasSize(8);
                        assertThat(context.getBeansOfType(HttpClientBuilder.class).values()).allSatisfy(httpClientBuilder -> {
                            SpringRestClientBuilder springRestClientBuilder = (SpringRestClientBuilder) httpClientBuilder;
                            assertThat(springRestClientBuilder.restClientBuilder())
                                    .isNotSameAs(context.getBean("firstRestClientBuilder"))
                                    .isNotSameAs(context.getBean("secondRestClientBuilder"));
                            assertThat(springRestClientBuilder.webClientBuilder().webClientBuilder())
                                    .isNotSameAs(context.getBean("firstWebClientBuilder"))
                                    .isNotSameAs(context.getBean("secondWebClientBuilder"));
                        });

                        assertThat(appender.list)
                                .filteredOn(event -> event.getLevel() == Level.WARN)
                                .extracting(ILoggingEvent::getFormattedMessage)
                                .anySatisfy(message -> assertThat(message)
                                        .contains(RestClient.Builder.class.getName())
                                        .contains("firstRestClientBuilder, secondRestClientBuilder"))
                                .anySatisfy(message -> assertThat(message)
                                        .contains(WebClient.Builder.class.getName())
                                        .contains("firstWebClientBuilder, secondWebClientBuilder"));
                    });
        } finally {
            logger.detachAppender(appender);
        }
    }
}
