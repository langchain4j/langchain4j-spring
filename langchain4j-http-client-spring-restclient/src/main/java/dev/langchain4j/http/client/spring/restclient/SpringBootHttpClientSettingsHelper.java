package dev.langchain4j.http.client.spring.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.time.Duration;

/**
 * Factory that detects the Spring Boot version on the classpath and delegates
 * {@link ClientHttpRequestFactory} creation to the appropriate strategy.
 */
public final class SpringBootHttpClientSettingsHelper {

    private static final Logger log = LoggerFactory.getLogger(SpringBootHttpClientSettingsHelper.class);

    private static final HttpClientSettingsStrategy STRATEGY;

    static {
        String version = SpringBootVersion.getVersion();
        int majorVersion = Integer.parseInt(version.split("\\.")[0]);
        log.debug("Detected Spring Boot major version {}", majorVersion);

        HttpClientSettingsStrategy detected;
        if (majorVersion >= 4) {
            detected = new SpringBoot4HttpClientSettings();
        } else {
            detected = new SpringBoot3HttpClientSettings();
        }
        STRATEGY = detected;
    }

    private SpringBootHttpClientSettingsHelper() {
    }

    public static ClientHttpRequestFactory createClientHttpRequestFactory(Duration connectTimeout, Duration readTimeout) {
        return STRATEGY.createRequestFactory(connectTimeout, readTimeout);
    }
}
