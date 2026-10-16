package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import reactor.adapter.JdkFlowAdapter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.langchain4j.http.client.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringRestClientTest {

    @Test
    void should_not_change_the_rest_client_builder_passed_in() {
        AtomicBoolean requestFactoryUsed = new AtomicBoolean();
        ClientHttpRequestFactory requestFactory = (uri, method) -> {
            requestFactoryUsed.set(true);
            throw new IOException("not sent");
        };
        RestClient.Builder restClientBuilder = RestClient.builder().requestFactory(requestFactory);

        SpringRestClient.builder().restClientBuilder(restClientBuilder).build();

        assertThatThrownBy(() -> restClientBuilder.build().get().uri("http://localhost/").retrieve().toBodilessEntity());
        assertThat(requestFactoryUsed).isTrue();
    }

    @Test
    void should_fail_the_future_when_the_web_client_cannot_be_built() {
        SpringRestClient client = SpringRestClient.builder()
                .clientHttpConnectorBuilder(settings -> {
                    throw new IllegalStateException("broken connector");
                })
                .build();

        CompletableFuture<SuccessfulHttpResponse> future = client.executeAsync(request());

        assertThat(future)
                .failsWithin(Duration.ofSeconds(10))
                .withThrowableThat()
                .havingCause()
                .isInstanceOf(IllegalStateException.class)
                .withMessage("broken connector");
    }

    @Test
    void should_fail_the_publisher_when_the_web_client_cannot_be_built() {
        SpringRestClient client = SpringRestClient.builder()
                .clientHttpConnectorBuilder(settings -> {
                    throw new IllegalStateException("broken connector");
                })
                .build();

        Flow.Publisher<HttpStreamingEvent> publisher = client.stream(request());

        assertThatThrownBy(() -> JdkFlowAdapter.flowPublisherToFlux(publisher).blockLast(Duration.ofSeconds(10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("broken connector");
    }

    private static HttpRequest request() {
        return HttpRequest.builder()
                .method(GET)
                .url("http://localhost/")
                .build();
    }
}
