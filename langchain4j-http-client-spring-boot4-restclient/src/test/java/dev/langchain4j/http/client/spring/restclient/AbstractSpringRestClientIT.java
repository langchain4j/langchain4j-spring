package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientIT;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import java.util.List;

/**
 * {@link SpringRestClient} builds its own request factory, so which HTTP client is used depends on what is on the
 * classpath. Subclasses pin one factory each, so that a failure says which client it came from rather than
 * pointing at a loop over all of them.
 */
abstract class AbstractSpringRestClientIT extends HttpClientIT {

    protected abstract ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder();

    @Override
    protected List<HttpClient> clients() {
        return List.of(SpringRestClient.builder()
                .clientHttpRequestFactoryBuilder(clientHttpRequestFactoryBuilder())
                .build());
    }
}
