package dev.langchain4j.autoconfigure.http;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;

/**
 * Callback to customize the {@link HttpClientBuilder}
 * used to create the auto-configured {@link HttpClient}.
 */
@FunctionalInterface
public interface HttpClientBuilderCustomizer {

    void customize(HttpClientBuilder builder);

}
