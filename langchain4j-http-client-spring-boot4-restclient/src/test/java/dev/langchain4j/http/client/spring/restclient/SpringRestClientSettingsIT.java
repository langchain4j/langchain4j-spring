package dev.langchain4j.http.client.spring.restclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import reactor.adapter.JdkFlowAdapter;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static dev.langchain4j.http.client.HttpMethod.GET;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringRestClientSettingsIT {

    private static final Duration SHORT = Duration.ofMillis(300);
    private static final Duration LONG = Duration.ofSeconds(10);

    private WireMockServer wireMockServer;

    @BeforeEach
    void startServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        wireMockServer.stubFor(get(urlEqualTo("/redirect"))
                .willReturn(aResponse().withStatus(302).withHeader("Location", "/target")));
        wireMockServer.stubFor(get(urlEqualTo("/target")).willReturn(aResponse().withStatus(200).withBody("target")));
        wireMockServer.stubFor(get(urlEqualTo("/slow")).willReturn(aResponse().withFixedDelay(1_000).withBody("slow")));
    }

    @AfterEach
    void stopServer() {
        wireMockServer.stop();
    }

    static Stream<Arguments> requestFactories() {
        return Stream.of(
                Arguments.of("jdk", ClientHttpRequestFactoryBuilder.jdk()),
                Arguments.of("http-components", ClientHttpRequestFactoryBuilder.httpComponents()),
                Arguments.of("simple", ClientHttpRequestFactoryBuilder.simple()));
    }

    static Stream<Arguments> connectors() {
        return Stream.of(
                Arguments.of("jdk", ClientHttpConnectorBuilder.jdk()),
                Arguments.of("reactor", ClientHttpConnectorBuilder.reactor()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestFactories")
    void should_apply_settings_on_blocking_calls(String name, ClientHttpRequestFactoryBuilder<?> requestFactory) {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(requestFactory)
                .httpClientSettings(HttpClientSettings.defaults()
                        .withRedirects(HttpRedirects.DONT_FOLLOW));

        assertThatThrownBy(() -> builder.build().execute(request("/redirect")));

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/redirect")));
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/target")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestFactories")
    void should_follow_redirects_without_settings_on_blocking_calls(
            String name, ClientHttpRequestFactoryBuilder<?> requestFactory) {

        SpringRestClientBuilder builder = SpringRestClient.builder().clientHttpRequestFactoryBuilder(requestFactory);

        SuccessfulHttpResponse response = builder.build().execute(request("/redirect"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("target");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestFactories")
    void should_apply_the_read_timeout_of_the_settings_on_blocking_calls(String name, ClientHttpRequestFactoryBuilder<?> requestFactory) {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(requestFactory)
                .httpClientSettings(HttpClientSettings.defaults().withReadTimeout(SHORT));

        assertThatThrownBy(() -> builder.build().execute(request("/slow")))
                .isExactlyInstanceOf(TimeoutException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestFactories")
    void should_prefer_the_read_timeout_of_the_builder_over_the_settings_on_blocking_calls(String name, ClientHttpRequestFactoryBuilder<?> requestFactory) {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(requestFactory)
                .httpClientSettings(HttpClientSettings.defaults().withReadTimeout(SHORT))
                .readTimeout(LONG);

        SuccessfulHttpResponse response = builder.build().execute(request("/slow"));

        assertThat(response.body()).isEqualTo("slow");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_apply_settings_on_non_blocking_calls(String name, ClientHttpConnectorBuilder<?> connector) {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpConnectorBuilder(connector)
                .httpClientSettings(HttpClientSettings.defaults()
                        .withRedirects(HttpRedirects.DONT_FOLLOW));

        assertThatThrownBy(() -> builder.build().executeAsync(request("/redirect")).get(10, SECONDS))
                .cause()
                .isInstanceOfSatisfying(HttpException.class, e -> assertThat(e.statusCode()).isEqualTo(302));

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/redirect")));
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/target")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_follow_redirects_without_settings_on_non_blocking_calls(
            String name, ClientHttpConnectorBuilder<?> connector) throws Exception {

        SpringRestClientBuilder builder = SpringRestClient.builder().clientHttpConnectorBuilder(connector);

        SuccessfulHttpResponse response = builder.build().executeAsync(request("/redirect")).get(10, SECONDS);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("target");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_apply_the_read_timeout_of_the_settings_on_non_blocking_calls(
            String name, ClientHttpConnectorBuilder<?> connector) {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpConnectorBuilder(connector)
                .httpClientSettings(HttpClientSettings.defaults().withReadTimeout(SHORT));

        assertThatThrownBy(() -> builder.build().executeAsync(request("/slow")).get(10, SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isExactlyInstanceOf(TimeoutException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_prefer_the_read_timeout_of_the_builder_over_the_settings_on_non_blocking_calls(
            String name, ClientHttpConnectorBuilder<?> connector) throws Exception {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpConnectorBuilder(connector)
                .httpClientSettings(HttpClientSettings.defaults().withReadTimeout(SHORT))
                .readTimeout(LONG);

        SuccessfulHttpResponse response = builder.build().executeAsync(request("/slow")).get(10, SECONDS);

        assertThat(response.body()).isEqualTo("slow");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestFactories")
    void should_apply_settings_on_blocking_streaming_calls(String name, ClientHttpRequestFactoryBuilder<?> requestFactory)
            throws Exception {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(requestFactory)
                .httpClientSettings(HttpClientSettings.defaults().withRedirects(HttpRedirects.DONT_FOLLOW));

        assertThat(blockingStream(builder, "/redirect").get(10, SECONDS))
                .isInstanceOfSatisfying(HttpException.class, e -> assertThat(e.statusCode()).isEqualTo(302));

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/redirect")));
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/target")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestFactories")
    void should_follow_redirects_without_settings_on_blocking_streaming_calls(
            String name, ClientHttpRequestFactoryBuilder<?> requestFactory) throws Exception {

        SpringRestClientBuilder builder = SpringRestClient.builder().clientHttpRequestFactoryBuilder(requestFactory);

        assertThat(blockingStream(builder, "/redirect").get(10, SECONDS)).isNull();

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/target")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_apply_settings_on_non_blocking_streaming_calls(String name, ClientHttpConnectorBuilder<?> connector) {

        SpringRestClientBuilder builder = SpringRestClient.builder()
                .clientHttpConnectorBuilder(connector)
                .httpClientSettings(HttpClientSettings.defaults().withRedirects(HttpRedirects.DONT_FOLLOW));

        assertThatThrownBy(() -> nonBlockingStream(builder, "/redirect"))
                .isInstanceOfSatisfying(HttpException.class, e -> assertThat(e.statusCode()).isEqualTo(302));

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/redirect")));
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/target")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_follow_redirects_without_settings_on_non_blocking_streaming_calls(
            String name, ClientHttpConnectorBuilder<?> connector) {

        SpringRestClientBuilder builder = SpringRestClient.builder().clientHttpConnectorBuilder(connector);

        nonBlockingStream(builder, "/redirect");

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/target")));
    }

    /**
     * Completes with the error of the stream, or with {@code null} when the stream closes without one.
     */
    private CompletableFuture<Throwable> blockingStream(SpringRestClientBuilder builder, String path) {
        CompletableFuture<Throwable> result = new CompletableFuture<>();
        builder.build().execute(request(path), new ServerSentEventListener() {

            @Override
            public void onError(Throwable error) {
                result.complete(error);
            }

            @Override
            public void onClose() {
                result.complete(null);
            }
        });
        return result;
    }

    private List<HttpStreamingEvent> nonBlockingStream(SpringRestClientBuilder builder, String path) {
        return JdkFlowAdapter.flowPublisherToFlux(builder.build().stream(request(path)))
                .collectList()
                .block(Duration.ofSeconds(10));
    }

    private HttpRequest request(String path) {
        return HttpRequest.builder()
                .method(GET)
                .url("http://localhost:" + wireMockServer.port() + path)
                .build();
    }
}
