package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.AbstractHttpClientPublisherNonBlockingIT;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.log.LoggingHttpClient;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;

class SpringRestClientReactorPublisherNonBlockingIT extends AbstractHttpClientPublisherNonBlockingIT {

    @Override
    protected HttpClient newClient(boolean logging) {
        HttpClient client = SpringRestClient.builder()
                .clientHttpConnectorBuilder(ClientHttpConnectorBuilder.reactor())
                .build();
        return logging ? new LoggingHttpClient(client, true, true) : client;
    }

    @Override
    protected String policedThreadNamePrefix() {
        return "reactor-http-";
    }
}
