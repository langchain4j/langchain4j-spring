package dev.langchain4j.http.client.spring.restclient;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.time.Duration;

class SpringBoot4HttpClientSettings implements HttpClientSettingsStrategy {

    @Override
    public ClientHttpRequestFactory createRequestFactory(Duration connectTimeout, Duration readTimeout) {
        HttpClientSettings settings = HttpClientSettings.defaults();
        if (connectTimeout != null) {
            settings = settings.withConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            settings = settings.withReadTimeout(readTimeout);
        }
        return ClientHttpRequestFactoryBuilder.detect()
                .build(settings);
    }
}
