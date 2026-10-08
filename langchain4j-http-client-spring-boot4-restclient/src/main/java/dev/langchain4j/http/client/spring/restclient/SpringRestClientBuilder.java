package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.Experimental;
import dev.langchain4j.http.client.HttpClientBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class SpringRestClientBuilder implements HttpClientBuilder {

    private RestClient.Builder restClientBuilder;
    private ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder;
    private WebClientBuilderHolder webClientBuilder;
    private ClientHttpConnectorBuilder<?> clientHttpConnectorBuilder;
    private AsyncTaskExecutor streamingRequestExecutor;
    private Boolean createDefaultStreamingRequestExecutor = true;
    private Duration connectTimeout;
    private Duration readTimeout;

    public RestClient.Builder restClientBuilder() {
        return restClientBuilder;
    }

    public SpringRestClientBuilder restClientBuilder(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
        return this;
    }

    public ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder() {
        return clientHttpRequestFactoryBuilder;
    }

    /**
     * Selects the {@link org.springframework.http.client.ClientHttpRequestFactory} to use. When not set, Spring
     * picks one by looking at what is on the classpath, which means the underlying HTTP client depends on the
     * dependencies of the application. Set this to pin a specific client.
     * <p>
     * Note that the request factory of a {@link #restClientBuilder(RestClient.Builder)} is not used: the timeouts
     * configured on this builder have to be applied to the factory, so it is always built here.
     */
    public SpringRestClientBuilder clientHttpRequestFactoryBuilder(
            ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder) {
        this.clientHttpRequestFactoryBuilder = clientHttpRequestFactoryBuilder;
        return this;
    }

    @Experimental
    public WebClientBuilderHolder webClientBuilder() {
        return webClientBuilder;
    }

    /**
     * Sets the {@code WebClient.Builder} used for the non-blocking methods,
     * {@link SpringRestClient#executeAsync(dev.langchain4j.http.client.HttpRequest)} and
     * {@link SpringRestClient#stream(dev.langchain4j.http.client.HttpRequest, dev.langchain4j.http.client.sse.ServerSentEventParser)},
     * wrapped in a {@link WebClientBuilderHolder}: {@code webClientBuilder(WebClientBuilderHolder.of(WebClient.builder()))}.
     * The blocking methods always use {@link RestClient}.
     * <p>
     * When not set, {@code WebClient.builder()} is used. The given builder is copied, so it is not modified. Its
     * filters, default headers and observation settings apply, but its connector is not used: the timeouts
     * configured on this builder have to be applied to the connector, so it is always built here (see
     * {@link #clientHttpConnectorBuilder(ClientHttpConnectorBuilder)}), as is the request factory of the
     * {@link #restClientBuilder(RestClient.Builder)} (see {@link #clientHttpRequestFactoryBuilder(ClientHttpRequestFactoryBuilder)}).
     */
    @Experimental
    public SpringRestClientBuilder webClientBuilder(WebClientBuilderHolder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        return this;
    }

    @Experimental
    public ClientHttpConnectorBuilder<?> clientHttpConnectorBuilder() {
        return clientHttpConnectorBuilder;
    }

    /**
     * Selects the {@link org.springframework.http.client.reactive.ClientHttpConnector} used by the non-blocking
     * methods, {@link SpringRestClient#executeAsync(dev.langchain4j.http.client.HttpRequest)} and
     * {@link SpringRestClient#stream(dev.langchain4j.http.client.HttpRequest, dev.langchain4j.http.client.sse.ServerSentEventParser)},
     * which are sent with Spring's {@code WebClient} and therefore need {@code spring-webflux} on the classpath.
     * When not set, Spring picks a connector by looking at what is on the classpath (Reactor Netty, Jetty, Apache
     * HttpComponents, or the JDK client as a fallback). Set this to pin a specific client.
     */
    @Experimental
    public SpringRestClientBuilder clientHttpConnectorBuilder(ClientHttpConnectorBuilder<?> clientHttpConnectorBuilder) {
        this.clientHttpConnectorBuilder = clientHttpConnectorBuilder;
        return this;
    }

    public AsyncTaskExecutor streamingRequestExecutor() {
        return streamingRequestExecutor;
    }

    public SpringRestClientBuilder streamingRequestExecutor(AsyncTaskExecutor streamingRequestExecutor) {
        this.streamingRequestExecutor = streamingRequestExecutor;
        return this;
    }

    public Boolean createDefaultStreamingRequestExecutor() {
        return createDefaultStreamingRequestExecutor;
    }

    public SpringRestClientBuilder createDefaultStreamingRequestExecutor(Boolean createDefaultStreamingRequestExecutor) {
        this.createDefaultStreamingRequestExecutor = createDefaultStreamingRequestExecutor;
        return this;
    }

    @Override
    public Duration connectTimeout() {
        return connectTimeout;
    }

    @Override
    public SpringRestClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public Duration readTimeout() {
        return readTimeout;
    }

    @Override
    public SpringRestClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public SpringRestClient build() {
        return new SpringRestClient(this);
    }
}
