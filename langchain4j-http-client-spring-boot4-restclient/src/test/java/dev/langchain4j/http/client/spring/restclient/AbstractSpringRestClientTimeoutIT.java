package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientTimeoutIT;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import java.time.Duration;
import java.util.List;

/**
 * {@link SpringRestClient} builds its own request factory so that it can apply the configured timeouts, which
 * means a factory set on a {@link org.springframework.web.client.RestClient.Builder} is not used. Subclasses pin
 * one factory each through {@link SpringRestClientBuilder#clientHttpRequestFactoryBuilder}, so that every client
 * Spring can pick is actually covered - each reports a read timeout with a different exception underneath.
 */
abstract class AbstractSpringRestClientTimeoutIT extends HttpClientTimeoutIT {

    protected abstract ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder();

    @Override
    protected List<HttpClient> clients(Duration readTimeout) {
        return List.of(SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(clientHttpRequestFactoryBuilder())
                .readTimeout(readTimeout)
                .build());
    }
}
