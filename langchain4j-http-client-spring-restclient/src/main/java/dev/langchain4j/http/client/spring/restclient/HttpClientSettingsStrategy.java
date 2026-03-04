package dev.langchain4j.http.client.spring.restclient;

import org.springframework.http.client.ClientHttpRequestFactory;

import java.time.Duration;

/**
 * Strategy interface for creating a {@link ClientHttpRequestFactory} with the appropriate
 * Spring Boot API, depending on the version available on the classpath.
 *
 * @see SpringBoot4HttpClientSettings
 * @see SpringBoot3HttpClientSettings
 */
interface HttpClientSettingsStrategy {

    ClientHttpRequestFactory createRequestFactory(Duration connectTimeout, Duration readTimeout);
}
