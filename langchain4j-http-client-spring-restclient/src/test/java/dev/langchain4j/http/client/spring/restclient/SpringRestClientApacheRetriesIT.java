package dev.langchain4j.http.client.spring.restclient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class SpringRestClientApacheRetriesIT {

    @Test
    void should_not_retry_when_apache_httpclient5_is_on_classpath(WireMockRuntimeInfo wmRuntimeInfo) {
        String path = "/apache-retries";
        wmRuntimeInfo.getWireMock().register(
                get(path).willReturn(aResponse().withStatus(503).withBody("unavailable"))
        );

        var client = SpringRestClient.builder().build();
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .url(wmRuntimeInfo.getHttpBaseUrl() + path)
                .build();

        assertThatThrownBy(() -> client.execute(request))
                .isInstanceOf(HttpException.class)
                .extracting(ex -> ((HttpException) ex).statusCode())
                .isEqualTo(503);

        verify(1, getRequestedFor(urlEqualTo(path)));
    }
}
