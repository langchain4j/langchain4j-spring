package dev.langchain4j.http.client.spring.restclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.langchain4j.http.client.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SpringRestClient} used to replace the request factory of every {@link RestClient.Builder} it was
 * given with one detected from the classpath, so a factory configured on the supplied builder was silently
 * ignored and there was no way to pin an HTTP client through the {@link RestClient.Builder} API. A factory is
 * now only built when this builder carries configuration that can only be applied to one: a pinned
 * {@link SpringRestClientBuilder#clientHttpRequestFactoryBuilder(ClientHttpRequestFactoryBuilder)}, or timeouts,
 * which Spring can only push into a factory it builds itself.
 */
class SpringRestClientRequestFactoryTest {

    private static final int WIREMOCK_PORT = 8084;

    private WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
        wireMockServer.start();
        wireMockServer.stubFor(
                WireMock.get("/endpoint").willReturn(WireMock.aResponse().withBody("response")));
    }

    @AfterEach
    void afterEach() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private ClientHttpRequestFactory recordingClientHttpRequestFactory(List<ClientHttpRequestFactory> invocations) {
        ClientHttpRequestFactory factory = ClientHttpRequestFactoryBuilder.simple()
                .build(HttpClientSettings.defaults());
        return new ClientHttpRequestFactory() {

            @Override
            public ClientHttpRequest createRequest(URI uri, org.springframework.http.HttpMethod httpMethod)
                    throws IOException {
                invocations.add(factory);
                return factory.createRequest(uri, httpMethod);
            }
        };
    }

    @Test
    void should_use_the_request_factory_of_the_supplied_rest_client_builder() {

        List<ClientHttpRequestFactory> invocations = new CopyOnWriteArrayList<>();

        SpringRestClient client = SpringRestClient.builder()
                .restClientBuilder(RestClient.builder()
                        .requestFactory(recordingClientHttpRequestFactory(invocations)))
                .build();

        dev.langchain4j.http.client.SuccessfulHttpResponse response = client.execute(
                dev.langchain4j.http.client.HttpRequest.builder()
                        .method(GET)
                        .url(String.format("http://localhost:%s/endpoint", WIREMOCK_PORT))
                        .build());

        assertThat(invocations).hasSize(1); // the request went through the factory of the supplied builder
        assertThat(response.body()).isEqualTo("response");
    }

    @Test
    void should_build_the_request_factory_when_timeouts_are_configured() {

        // timeouts can only be applied to a request factory built by SpringRestClient itself,
        // so the factory of the supplied builder is ignored when one is set

        List<ClientHttpRequestFactory> invocations = new CopyOnWriteArrayList<>();

        SpringRestClient client = SpringRestClient.builder()
                .restClientBuilder(RestClient.builder()
                        .requestFactory(recordingClientHttpRequestFactory(invocations)))
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        dev.langchain4j.http.client.SuccessfulHttpResponse response = client.execute(
                dev.langchain4j.http.client.HttpRequest.builder()
                        .method(GET)
                        .url(String.format("http://localhost:%s/endpoint", WIREMOCK_PORT))
                        .build());

        assertThat(invocations).isEmpty(); // the request went through the built (detected) factory
        assertThat(response.body()).isEqualTo("response");
    }
}
