package dev.langchain4j.http.client.spring.restclient;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorSettings;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.http.HttpClient.Version.HTTP_2;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpringRestClientHttpVersionTest {

    private static final String BODY = "{\"message\":\"hello\"}";

    private HttpServer server;
    private final CompletableFuture<Headers> requestHeaders = new CompletableFuture<>();
    private final CompletableFuture<String> requestBody = new CompletableFuture<>();

    @BeforeEach
    void beforeEach() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/endpoint", exchange -> {
            requestHeaders.complete(exchange.getRequestHeaders());
            requestBody.complete(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
    }

    @AfterEach
    void afterEach() {
        server.stop(0);
    }

    @Test
    void should_not_request_h2c_upgrade_for_plain_http_when_jdk_client_is_detected() throws Exception {

        // given
        ClientHttpRequestFactory requestFactory = SpringRestClient.createRequestFactory(
                null, ClientHttpRequestFactoryBuilder::jdk, ClientHttpRequestFactorySettings.defaults());

        // when
        post(requestFactory);

        // then
        Headers headers = requestHeaders.get(5, SECONDS);
        assertThat(headers.containsKey("Upgrade")).isFalse();
        assertThat(headers.containsKey("HTTP2-Settings")).isFalse();
        assertThat(requestBody.get(5, SECONDS)).isEqualTo(BODY);
    }

    @Test
    void should_keep_http_version_of_custom_jdk_request_factory_builder() throws Exception {

        // given
        ClientHttpRequestFactory requestFactory = SpringRestClient.createRequestFactory(
                ClientHttpRequestFactoryBuilder.jdk().withHttpClientCustomizer(builder -> builder.version(HTTP_2)),
                ClientHttpRequestFactoryBuilder::detect,
                ClientHttpRequestFactorySettings.defaults());

        // when
        post(requestFactory);

        // then
        assertThat(requestHeaders.get(5, SECONDS).getFirst("Upgrade")).isEqualTo("h2c");
    }

    @Test
    void should_route_only_plain_http_to_http_1_1_request_factory() throws Exception {

        // given
        ClientHttpRequest defaultRequest = mock(ClientHttpRequest.class);
        ClientHttpRequest http11Request = mock(ClientHttpRequest.class);
        AtomicInteger http11FactoryCreations = new AtomicInteger();
        ClientHttpRequestFactory requestFactory = SpringRestClient.http11ForPlainHttp(
                (uri, method) -> defaultRequest,
                () -> {
                    http11FactoryCreations.incrementAndGet();
                    return (uri, method) -> http11Request;
                });

        // when-then
        assertThat(requestFactory.createRequest(URI.create("https://localhost/endpoint"), HttpMethod.POST))
                .isSameAs(defaultRequest);
        assertThat(http11FactoryCreations).hasValue(0);

        assertThat(requestFactory.createRequest(URI.create("http://localhost/endpoint"), HttpMethod.POST))
                .isSameAs(http11Request);
        assertThat(requestFactory.createRequest(URI.create("HTTP://localhost/endpoint"), HttpMethod.POST))
                .isSameAs(http11Request);
    }

    @Test
    void should_not_request_h2c_upgrade_for_plain_http_when_jdk_connector_is_detected() throws Exception {

        // given
        ClientHttpConnector connector = WebClientDelegate.createConnector(
                null, ClientHttpConnectorBuilder::jdk, ClientHttpConnectorSettings.defaults());

        // when
        post(connector);

        // then
        Headers headers = requestHeaders.get(5, SECONDS);
        assertThat(headers.containsKey("Upgrade")).isFalse();
        assertThat(headers.containsKey("HTTP2-Settings")).isFalse();
        assertThat(requestBody.get(5, SECONDS)).isEqualTo(BODY);
    }

    @Test
    void should_keep_http_version_of_custom_jdk_connector_builder() throws Exception {

        // given
        ClientHttpConnector connector = WebClientDelegate.createConnector(
                ClientHttpConnectorBuilder.jdk().withHttpClientCustomizer(builder -> builder.version(HTTP_2)),
                ClientHttpConnectorBuilder::detect,
                ClientHttpConnectorSettings.defaults());

        // when
        post(connector);

        // then
        assertThat(requestHeaders.get(5, SECONDS).getFirst("Upgrade")).isEqualTo("h2c");
    }

    @Test
    void should_route_only_plain_http_to_http_1_1_connector() {

        // given
        Mono<ClientHttpResponse> defaultResponse = Mono.empty();
        Mono<ClientHttpResponse> http11Response = Mono.empty();
        AtomicInteger http11ConnectorCreations = new AtomicInteger();
        ClientHttpConnector connector = WebClientDelegate.http11ForPlainHttp(
                (method, uri, requestCallback) -> defaultResponse,
                () -> {
                    http11ConnectorCreations.incrementAndGet();
                    return (method, uri, requestCallback) -> http11Response;
                });

        // when-then
        assertThat(connector.connect(HttpMethod.POST, URI.create("https://localhost/endpoint"), request -> Mono.empty()))
                .isSameAs(defaultResponse);
        assertThat(http11ConnectorCreations).hasValue(0);

        assertThat(connector.connect(HttpMethod.POST, URI.create("http://localhost/endpoint"), request -> Mono.empty()))
                .isSameAs(http11Response);
        assertThat(connector.connect(HttpMethod.POST, URI.create("HTTP://localhost/endpoint"), request -> Mono.empty()))
                .isSameAs(http11Response);
    }

    private void post(ClientHttpRequestFactory requestFactory) {
        RestClient.builder()
                .requestFactory(requestFactory)
                .build()
                .post()
                .uri("http://localhost:" + server.getAddress().getPort() + "/endpoint")
                .header("Content-Type", "application/json")
                .body(BODY)
                .retrieve()
                .toBodilessEntity();
    }

    private void post(ClientHttpConnector connector) {
        WebClient.builder()
                .clientConnector(connector)
                .build()
                .post()
                .uri("http://localhost:" + server.getAddress().getPort() + "/endpoint")
                .header("Content-Type", "application/json")
                .bodyValue(BODY)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(5));
    }
}
