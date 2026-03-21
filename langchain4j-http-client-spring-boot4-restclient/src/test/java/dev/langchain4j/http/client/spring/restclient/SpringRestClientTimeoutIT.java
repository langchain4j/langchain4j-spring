package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientTimeoutIT;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;

class SpringRestClientTimeoutIT extends HttpClientTimeoutIT {

    @Override
    protected List<HttpClient> clients(Duration readTimeout) {
        return List.of(
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new JdkClientHttpRequestFactory()))
                        .readTimeout(readTimeout)
                        .build(),
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory()))
                        .readTimeout(readTimeout)
                        .build(),
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new ReactorClientHttpRequestFactory()))
                        .readTimeout(readTimeout)
                        .build(),
                SpringRestClient.builder()
                        .restClientBuilder(RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory()))
                        .readTimeout(readTimeout)
                        .build()
        );
    }

    @Override
    protected Class<? extends Exception> expectedReadTimeoutRootCauseExceptionType() {
        return SocketTimeoutException.class;
    }
}
