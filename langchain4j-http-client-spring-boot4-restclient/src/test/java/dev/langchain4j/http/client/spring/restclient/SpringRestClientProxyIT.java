package dev.langchain4j.http.client.spring.restclient;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringRestClientProxyIT {

    @Test
    void should_route_requests_through_configured_http_proxy() throws IOException {
        AtomicReference<String> requestedUri = new AtomicReference<>();

        HttpServer proxyServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        proxyServer.createContext("/", exchange -> {
            requestedUri.set(exchange.getRequestURI().toString());

            byte[] body = "proxied".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            }
        });
        proxyServer.start();

        try {
            int proxyPort = proxyServer.getAddress().getPort();
            SpringRestClient client = SpringRestClient.builder()
                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyPort)))
                    .createDefaultStreamingRequestExecutor(false)
                    .build();
            HttpRequest request = HttpRequest.builder()
                    .method(HttpMethod.GET)
                    .url("http://example.com/proxy-test")
                    .build();

            SuccessfulHttpResponse response = client.execute(request);

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("proxied");
            assertThat(requestedUri.get()).contains("proxy-test");
        } finally {
            proxyServer.stop(0);
        }
    }

    @Test
    void should_reject_non_http_proxy() {
        assertThatThrownBy(() -> SpringRestClient.builder()
                .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 1080)))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only HTTP proxies are supported");
    }
}
