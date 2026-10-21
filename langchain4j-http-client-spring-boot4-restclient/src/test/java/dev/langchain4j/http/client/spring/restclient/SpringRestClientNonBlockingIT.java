package dev.langchain4j.http.client.spring.restclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.FormDataFile;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.HttpResponseReceived;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import reactor.adapter.JdkFlowAdapter;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static dev.langchain4j.http.client.HttpMethod.GET;
import static dev.langchain4j.http.client.HttpMethod.POST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringRestClientNonBlockingIT {

    private WireMockServer wireMockServer;

    @BeforeEach
    void startServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @AfterEach
    void stopServer() {
        wireMockServer.stop();
    }

    static Stream<Arguments> connectors() {
        return Stream.of(
                Arguments.of("jdk", ClientHttpConnectorBuilder.jdk()),
                Arguments.of("reactor", ClientHttpConnectorBuilder.reactor()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_return_successful_response(String name, ClientHttpConnectorBuilder<?> connector) throws Exception {

        // given
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withHeader("X-Custom", equalTo("value"))
                .withRequestBody(equalTo("{\"hello\":\"world\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"answer\":42}")));

        HttpRequest request = HttpRequest.builder()
                .method(POST)
                .url(url("/endpoint"))
                .addHeader("X-Custom", "value")
                .body("{\"hello\":\"world\"}")
                .build();

        // when
        SuccessfulHttpResponse response = client(connector, null).executeAsync(request).get(10, SECONDS);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().get("Content-Type")).containsExactly("application/json");
        assertThat(response.body()).isEqualTo("{\"answer\":42}");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_return_large_response(String name, ClientHttpConnectorBuilder<?> connector) throws Exception {

        // given
        String largeBody = "x".repeat(2 * 1024 * 1024);
        wireMockServer.stubFor(get(urlEqualTo("/large")).willReturn(aResponse().withStatus(200).withBody(largeBody)));

        HttpRequest request = HttpRequest.builder().method(GET).url(url("/large")).build();

        // when
        SuccessfulHttpResponse response = client(connector, null).executeAsync(request).get(10, SECONDS);

        // then
        assertThat(response.body()).hasSize(largeBody.length());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_fail_with_http_exception_carrying_large_error_body(String name, ClientHttpConnectorBuilder<?> connector) {

        // given
        String largeErrorBody = "e".repeat(1024 * 1024);
        wireMockServer.stubFor(get(urlEqualTo("/large-error")).willReturn(aResponse().withStatus(502).withBody(largeErrorBody)));

        HttpRequest request = HttpRequest.builder().method(GET).url(url("/large-error")).build();

        // when-then
        assertThatThrownBy(() -> client(connector, null).executeAsync(request).get(10, SECONDS))
                .cause()
                .isExactlyInstanceOf(HttpException.class)
                .satisfies(e -> assertThat(((HttpException) e).statusCode()).isEqualTo(502));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_return_response_without_body(String name, ClientHttpConnectorBuilder<?> connector) throws Exception {

        // given
        wireMockServer.stubFor(get(urlEqualTo("/empty")).willReturn(aResponse().withStatus(204)));

        HttpRequest request = HttpRequest.builder().method(GET).url(url("/empty")).build();

        // when
        SuccessfulHttpResponse response = client(connector, null).executeAsync(request).get(10, SECONDS);

        // then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_fail_with_http_exception_carrying_error_body(String name, ClientHttpConnectorBuilder<?> connector) {

        // given
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse().withStatus(400).withBody("{\"error\":\"bad request\"}")));

        HttpRequest request = HttpRequest.builder().method(POST).url(url("/endpoint")).body("{}").build();

        // when-then
        assertThatThrownBy(() -> client(connector, null).executeAsync(request).get(10, SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isExactlyInstanceOf(HttpException.class)
                .hasMessage("{\"error\":\"bad request\"}")
                .satisfies(e -> assertThat(((HttpException) e).statusCode()).isEqualTo(400));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_fail_with_timeout_exception(String name, ClientHttpConnectorBuilder<?> connector) {

        // given
        wireMockServer.stubFor(get(urlEqualTo("/slow")).willReturn(aResponse().withFixedDelay(2_000)));

        HttpRequest request = HttpRequest.builder().method(GET).url(url("/slow")).build();

        // when-then
        assertThatThrownBy(() -> client(connector, Duration.ofMillis(300)).executeAsync(request).get(10, SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isExactlyInstanceOf(TimeoutException.class);
    }

    static Stream<Arguments> formDataConnectors() {
        return Stream.of(
                // Against a plain-http server, the JDK client in its default HTTP/2 mode fails to send a streamed
                // multipart body (ClosedChannelException); over HTTP/1.1 it works. Real APIs are reached over https.
                Arguments.of("jdk", ClientHttpConnectorBuilder.jdk()
                        .withHttpClientCustomizer(builder -> builder.version(java.net.http.HttpClient.Version.HTTP_1_1))),
                Arguments.of("reactor", ClientHttpConnectorBuilder.reactor()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("formDataConnectors")
    void should_send_form_data(String name, ClientHttpConnectorBuilder<?> connector) throws Exception {

        // given
        wireMockServer.stubFor(post(urlEqualTo("/upload")).willReturn(aResponse().withStatus(200).withBody("ok")));

        HttpRequest request = HttpRequest.builder()
                .method(POST)
                .url(url("/upload"))
                .addFormDataField("purpose", "test")
                .addFormDataFile("file", "hello.txt", "text/plain", "hello".getBytes())
                .build();

        // when
        SuccessfulHttpResponse response = client(connector, null).executeAsync(request).get(10, SECONDS);

        // then
        assertThat(response.body()).isEqualTo("ok");
        wireMockServer.verify(postRequestedFor(urlEqualTo("/upload"))
                .withRequestBodyPart(aMultipart("purpose").withBody(equalTo("test")).build())
                .withRequestBodyPart(aMultipart("file").withBody(equalTo("hello")).build()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_stream_response_head_then_events(String name, ClientHttpConnectorBuilder<?> connector) {

        // given: the last event is not followed by a blank line, so it is only emitted when the body ends
        wireMockServer.stubFor(post(urlEqualTo("/sse")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: one\n\nevent: custom\ndata: two\n\ndata: three\n")));

        HttpRequest request = HttpRequest.builder().method(POST).url(url("/sse")).body("{}").build();

        // when
        List<HttpStreamingEvent> events = collect(client(connector, null).stream(request));

        // then
        assertThat(events.get(0)).isInstanceOfSatisfying(HttpResponseReceived.class, head -> {
            assertThat(head.response().statusCode()).isEqualTo(200);
            assertThat(head.response().headers().get("Content-Type")).containsExactly("text/event-stream");
        });
        assertThat(events.subList(1, events.size()))
                .extracting(event -> ((ServerSentEvent) event).data())
                .containsExactly("one", "two", "three");
        assertThat(((ServerSentEvent) events.get(2)).event()).isEqualTo("custom");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_fail_stream_with_http_exception_carrying_error_body(String name, ClientHttpConnectorBuilder<?> connector) {

        // given
        wireMockServer.stubFor(post(urlEqualTo("/sse"))
                .willReturn(aResponse().withStatus(429).withBody("{\"error\":\"rate limited\"}")));

        HttpRequest request = HttpRequest.builder().method(POST).url(url("/sse")).body("{}").build();

        // when-then
        assertThatThrownBy(() -> collect(client(connector, null).stream(request)))
                .isExactlyInstanceOf(HttpException.class)
                .hasMessage("{\"error\":\"rate limited\"}")
                .satisfies(e -> assertThat(((HttpException) e).statusCode()).isEqualTo(429));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_fail_stream_with_timeout_exception(String name, ClientHttpConnectorBuilder<?> connector) {

        // given
        wireMockServer.stubFor(post(urlEqualTo("/sse")).willReturn(aResponse().withFixedDelay(2_000)));

        HttpRequest request = HttpRequest.builder().method(POST).url(url("/sse")).body("{}").build();

        // when-then
        assertThatThrownBy(() -> collect(client(connector, Duration.ofMillis(300)).stream(request)))
                .isExactlyInstanceOf(TimeoutException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_fail_with_status_text_when_error_body_is_empty(String name, ClientHttpConnectorBuilder<?> connector) {

        // given
        wireMockServer.stubFor(post(urlEqualTo("/endpoint")).willReturn(aResponse().withStatus(400)));

        HttpRequest request = HttpRequest.builder().method(POST).url(url("/endpoint")).body("{}").build();

        // when-then: the same message as on the blocking path
        assertThatThrownBy(() -> client(connector, null).executeAsync(request).get(10, SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isExactlyInstanceOf(HttpException.class)
                .hasMessage("Bad Request");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("connectors")
    void should_stream_with_a_custom_parser(String name, ClientHttpConnectorBuilder<?> connector) {

        // given: newline-delimited JSON, as Ollama streams it, sent in small chunks that split lines; the last line
        // has no trailing newline
        wireMockServer.stubFor(post(urlEqualTo("/ndjson")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/x-ndjson")
                .withBody("{\"text\":\"Hel\"}\n{\"text\":\"lo\"}\n{\"done\":true}")
                .withChunkedDribbleDelay(10, 200)));

        HttpRequest request = HttpRequest.builder().method(POST).url(url("/ndjson")).body("{}").build();

        // when
        List<HttpStreamingEvent> events = collect(client(connector, null).stream(request, new LineParser()));

        // then
        assertThat(events.subList(1, events.size()))
                .extracting(event -> ((ServerSentEvent) event).data())
                .containsExactly("{\"text\":\"Hel\"}", "{\"text\":\"lo\"}", "{\"done\":true}");
    }

    /**
     * Emits every line as an event, like the parsers of providers that stream newline-delimited JSON.
     */
    private static class LineParser implements ServerSentEventParser {

        @Override
        public void parse(InputStream httpResponseBody, ServerSentEventListener listener) {
            throw new UnsupportedOperationException("only the incremental parser is used by non-blocking streams");
        }

        @Override
        public Incremental incremental() {
            return new Incremental() {

                private final ByteArrayOutputStream line = new ByteArrayOutputStream();

                @Override
                public List<ServerSentEvent> feed(ByteBuffer bytes) {
                    List<ServerSentEvent> events = new ArrayList<>();
                    while (bytes.hasRemaining()) {
                        byte b = bytes.get();
                        if (b == '\n') {
                            events.add(new ServerSentEvent(null, line.toString(StandardCharsets.UTF_8)));
                            line.reset();
                        } else {
                            line.write(b);
                        }
                    }
                    return events;
                }

                @Override
                public List<ServerSentEvent> flush() {
                    return line.size() == 0
                            ? List.of()
                            : List.of(new ServerSentEvent(null, line.toString(StandardCharsets.UTF_8)));
                }
            };
        }
    }

    private static List<HttpStreamingEvent> collect(Flow.Publisher<HttpStreamingEvent> publisher) {
        return JdkFlowAdapter.flowPublisherToFlux(publisher).collectList().block(Duration.ofSeconds(10));
    }

    private static HttpClient client(ClientHttpConnectorBuilder<?> connector, Duration readTimeout) {
        return SpringRestClient.builder()
                .clientHttpConnectorBuilder(connector)
                .readTimeout(readTimeout)
                .build();
    }

    private String url(String path) {
        return "http://localhost:" + wireMockServer.port() + path;
    }
}
