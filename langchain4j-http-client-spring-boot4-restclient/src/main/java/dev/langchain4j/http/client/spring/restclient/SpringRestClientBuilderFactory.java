package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClientBuilderFactory;

public class SpringRestClientBuilderFactory implements HttpClientBuilderFactory {

    @Override
    public int priority() {
        return YIELDS_TO_OTHERS - 1; // a client built here does not use the HTTP settings of Spring Boot, so any other client is preferred
    }

    @Override
    public SpringRestClientBuilder create() {
        return SpringRestClient.builder();
    }
}
