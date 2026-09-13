package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.http.client.HttpClientBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.client.RestClient;

import java.net.Proxy;
import java.time.Duration;

public class SpringRestClientBuilder implements HttpClientBuilder {

    private RestClient.Builder restClientBuilder;
    private ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder;
    private AsyncTaskExecutor streamingRequestExecutor;
    private Boolean createDefaultStreamingRequestExecutor = true;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Proxy proxy;

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

    public Proxy proxy() {
        return proxy;
    }

    public SpringRestClientBuilder proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    @Override
    public SpringRestClient build() {
        return new SpringRestClient(this);
    }
}
