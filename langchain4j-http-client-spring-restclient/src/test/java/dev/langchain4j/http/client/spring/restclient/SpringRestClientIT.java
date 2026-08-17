package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientIT;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import java.util.List;

/**
 * Covers every request factory Spring can pick, because {@link SpringRestClient} builds the factory itself and a
 * factory set on a {@link org.springframework.web.client.RestClient.Builder} would be ignored.
 * <p>
 * Runs in the nightly build only, see {@code .github/workflows/nightly.yaml}.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class SpringRestClientIT extends HttpClientIT {

    @Override
    protected List<HttpClient> clients() {
        return List.of(
                client(ClientHttpRequestFactoryBuilder.jdk()),
                client(ClientHttpRequestFactoryBuilder.httpComponents()),
                client(ClientHttpRequestFactoryBuilder.reactor()),
                client(ClientHttpRequestFactoryBuilder.simple()));
    }

    private static HttpClient client(ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder) {
        return SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(clientHttpRequestFactoryBuilder)
                .build();
    }
}
