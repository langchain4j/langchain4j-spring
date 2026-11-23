package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientCancellationIT;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;

class SpringRestClientReactorCancellationIT extends HttpClientCancellationIT {

    @Override
    protected HttpClient client() {
        return SpringRestClient.builder()
                .clientHttpConnectorBuilder(ClientHttpConnectorBuilder.reactor())
                .build();
    }
}
