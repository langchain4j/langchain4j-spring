package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientPublisherIT;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;

import java.util.List;

class SpringRestClientPublisherIT extends HttpClientPublisherIT {

    @Override
    protected List<HttpClient> clients() {
        return List.of(
                SpringRestClient.builder().clientHttpConnectorBuilder(ClientHttpConnectorBuilder.jdk()).build(),
                SpringRestClient.builder().clientHttpConnectorBuilder(ClientHttpConnectorBuilder.reactor()).build());
    }
}
