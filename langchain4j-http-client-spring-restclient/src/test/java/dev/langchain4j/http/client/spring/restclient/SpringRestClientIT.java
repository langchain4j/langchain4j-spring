package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientIT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class SpringRestClientIT extends HttpClientIT {

    @Override
    protected List<HttpClient> clients() {
        return List.of(
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new JdkClientHttpRequestFactory()))
                        .build(),
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory()))
                        .build(),
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new ReactorClientHttpRequestFactory()))
                        .build(),
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory()))
                        .build()
        );
    }

    @Disabled
    @Override
    protected void should_return_successful_http_response_sync_form_data() {
        // TODO SpringRestClient does not support form data yet
    }
}
