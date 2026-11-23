package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.Experimental;
import dev.langchain4j.http.client.HttpClientBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class SpringRestClientBuilder implements HttpClientBuilder {

    private RestClient.Builder restClientBuilder;
    private ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder;
    private HttpClientSettings httpClientSettings;
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
     * When the JDK {@code HttpClient} is used, whether Spring picks it or it is set here, plain {@code http://}
     * requests are sent over HTTP/1.1, and {@code https://} requests use HTTP/2 when the server supports it. HTTP/2
     * over plain HTTP would require an {@code Upgrade: h2c} request, which many servers do not handle correctly (for
     * example, they ignore the request body).
     * <p>
     * Note that the request factory of a {@link #restClientBuilder(RestClient.Builder)} is not used: the timeouts
     * configured on this builder have to be applied to the factory, so it is always built here, with the settings
     * given to {@link #httpClientSettings(HttpClientSettings)}.
     */
    public SpringRestClientBuilder clientHttpRequestFactoryBuilder(
            ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder) {
        this.clientHttpRequestFactoryBuilder = clientHttpRequestFactoryBuilder;
        return this;
    }

    public HttpClientSettings httpClientSettings() {
        return httpClientSettings;
    }

    /**
     * Sets the Spring Boot HTTP client settings, such as redirects and the SSL bundle, that the request factory of
     * the blocking methods and the connector of the non-blocking methods are built with. When not set,
     * {@link HttpClientSettings#defaults()} is used.
     * <p>
     * The {@link #connectTimeout(Duration)} and {@link #readTimeout(Duration)} configured on this builder take
     * precedence over the timeouts of these settings.
     * LangChain4j models always set these timeouts, so the timeouts of these settings only apply to a client that
     * is used directly; set the timeout on the model instead.
     * <p>
     * Like the non-blocking methods themselves, how these settings apply to them is experimental.
     */
    public SpringRestClientBuilder httpClientSettings(HttpClientSettings httpClientSettings) {
        this.httpClientSettings = httpClientSettings;
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
     * HttpComponents, or the JDK client as a fallback). Set this to pin a specific client. The connector is built
     * with the settings given to {@link #httpClientSettings(HttpClientSettings)}.
     * <p>
     * The HTTP version is chosen as described in {@link #clientHttpRequestFactoryBuilder(ClientHttpRequestFactoryBuilder)}:
     * with the JDK client, plain {@code http://} requests are sent over HTTP/1.1.
     * <p>
     * The connector is built on the first non-blocking call, not when the client is built, so that a client that
     * only makes blocking calls does not start its I/O threads. A problem with the connector, such as an invalid SSL
     * bundle, therefore surfaces on that call, as a failed future or publisher.
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
