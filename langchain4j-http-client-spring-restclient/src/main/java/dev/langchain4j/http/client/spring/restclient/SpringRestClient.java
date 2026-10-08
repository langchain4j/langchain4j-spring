package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.FormDataFile;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static dev.langchain4j.http.client.sse.ServerSentEventListenerUtils.ignoringExceptions;
import static dev.langchain4j.internal.AsyncNotSupported.failedFuture;
import static dev.langchain4j.internal.AsyncNotSupported.failingPublisher;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.Utils.isNullOrBlank;

public class SpringRestClient implements HttpClient {

    private static final boolean WEBFLUX_PRESENT = ClassUtils.isPresent(
            "org.springframework.web.reactive.function.client.WebClient", SpringRestClient.class.getClassLoader());

    private static final String WEBFLUX_MISSING = "Non-blocking calls (for example AI Service methods returning"
            + " CompletableFuture, Flow.Publisher, Mono or Flux<AiServiceStreamingEvent>) need spring-webflux on the"
            + " classpath when they are sent with SpringRestClient: add the org.springframework:spring-webflux"
            + " dependency, and if the application is not a web application, also set"
            + " spring.main.web-application-type=none. See"
            + " https://docs.langchain4j.dev/tutorials/spring-boot-integration. %s() is not available without it.";

    private final RestClient delegate;
    private final AsyncTaskExecutor streamingRequestExecutor;
    private final WebClientDelegate webClientDelegate;

    public SpringRestClient(SpringRestClientBuilder builder) {

        RestClient.Builder restClientBuilder = getOrDefault(builder.restClientBuilder(), RestClient::builder);

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults();
        if (builder.connectTimeout() != null) {
            settings = settings.withConnectTimeout(builder.connectTimeout());
        }
        if (builder.readTimeout() != null) {
            settings = settings.withReadTimeout(builder.readTimeout());
        }
        ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder = getOrDefault(
                builder.clientHttpRequestFactoryBuilder(), ClientHttpRequestFactoryBuilder::detect);
        if (requestFactoryBuilder instanceof HttpComponentsClientHttpRequestFactoryBuilder httpComponentsBuilder) {
            requestFactoryBuilder = httpComponentsBuilder.withHttpClientCustomizer(HttpClientBuilder::disableAutomaticRetries);
        }
        ClientHttpRequestFactory clientHttpRequestFactory = requestFactoryBuilder.build(settings);

        this.delegate = restClientBuilder
                .requestFactory(clientHttpRequestFactory)
                .build();

        this.webClientDelegate = WEBFLUX_PRESENT ? new WebClientDelegate(builder) : null;

        this.streamingRequestExecutor = getOrDefault(builder.streamingRequestExecutor(), () -> {
            if (builder.createDefaultStreamingRequestExecutor()) {
                return createDefaultStreamingRequestExecutor();
            } else {
                return null;
            }
        });
    }

    private static AsyncTaskExecutor createDefaultStreamingRequestExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.initialize();
        return taskExecutor;
    }

    public static SpringRestClientBuilder builder() {
        return new SpringRestClientBuilder();
    }

    @Override
    public SuccessfulHttpResponse execute(HttpRequest request) throws HttpException {
        try {
            ResponseEntity<byte[]> responseEntity = toSpringRestClientRequest(request)
                    .retrieve()
                    .toEntity(byte[].class);

            return SuccessfulHttpResponse.builder()
                    .statusCode(responseEntity.getStatusCode().value())
                    .headers(responseEntity.getHeaders())
                    .body(responseEntity.getBody())
                    .build();
        } catch (RestClientResponseException e) {
            throw new HttpException(e.getStatusCode().value(), errorMessage(e.getResponseBodyAsString(), e.getMessage()));
        } catch (Exception e) {
            if (isTimeout(e)) {
                throw new TimeoutException(e);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
        streamingRequestExecutor.execute(() -> {
            try {
                toSpringRestClientRequest(request)
                        .exchange((springRequest, springResponse) -> {

                            int statusCode = springResponse.getStatusCode().value();

                            if (!springResponse.getStatusCode().is2xxSuccessful()) {
                                String body = springResponse.bodyTo(String.class);

                                HttpException exception =
                                        new HttpException(statusCode, errorMessage(body, springResponse.getStatusText()));
                                ignoringExceptions(() -> listener.onError(exception));
                                return null;
                            }

                            SuccessfulHttpResponse response = SuccessfulHttpResponse.builder()
                                    .statusCode(statusCode)
                                    .headers(springResponse.getHeaders())
                                    .build();
                            ignoringExceptions(() -> listener.onOpen(response));

                            try (InputStream inputStream = springResponse.getBody()) {
                                parser.parse(inputStream, listener);
                                ignoringExceptions(listener::onClose);
                            }

                            return null;
                        });
            } catch (Exception e) {
                if (isTimeout(e)) {
                    ignoringExceptions(() -> listener.onError(new TimeoutException(e)));
                } else {
                    ignoringExceptions(() -> listener.onError(e));
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     * <p>
     * The request is sent with Spring's {@code WebClient}, so this needs {@code spring-webflux} on the classpath;
     * without it, the returned future fails with an {@link dev.langchain4j.exception.AsyncNotSupportedException}.
     * The connector is chosen by {@link SpringRestClientBuilder#clientHttpConnectorBuilder}, and the response body
     * is read in full, without a size limit, as for {@link #execute(HttpRequest)}.
     */
    @Override
    public CompletableFuture<SuccessfulHttpResponse> executeAsync(HttpRequest request) {
        if (webClientDelegate == null) {
            return failedFuture(WEBFLUX_MISSING.formatted("executeAsync"));
        }
        return webClientDelegate.executeAsync(request);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The request is sent with Spring's {@code WebClient}, so this needs {@code spring-webflux} on the classpath;
     * without it, the publisher fails with an {@link dev.langchain4j.exception.AsyncNotSupportedException}.
     * Events are delivered as they arrive: the body is parsed incrementally as each chunk is received, and no thread
     * is held for the lifetime of the stream. Cancelling the subscription aborts the request.
     */
    @Override
    public Flow.Publisher<HttpStreamingEvent> stream(HttpRequest request, ServerSentEventParser parser) {
        if (webClientDelegate == null) {
            return failingPublisher(WEBFLUX_MISSING.formatted("stream"));
        }
        return webClientDelegate.stream(request, parser);
    }

    private RestClient.RequestBodySpec toSpringRestClientRequest(HttpRequest request) {
        RestClient.RequestBodySpec requestBodySpec = delegate
                .method(org.springframework.http.HttpMethod.valueOf(request.method().name()))
                .uri(request.url())
                .headers(httpHeaders -> httpHeaders.putAll(request.headers()));

        if (request.formDataFields().isEmpty() && request.formDataFiles().isEmpty()) {
            if (request.body() != null) {
                requestBodySpec.body(request.body());
            }
        } else {
            requestBodySpec.body(toMultiValueMap(request.formDataFields(), request.formDataFiles()));
        }

        return requestBodySpec;
    }

    private static MultiValueMap<String, Object> toMultiValueMap(Map<String, String> fields, Map<String, FormDataFile> files) {
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            multipart.add(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, FormDataFile> entry : files.entrySet()) {
            multipart.add(entry.getKey(), new ByteArrayResource(entry.getValue().content()) {
                @Override public String getFilename() { return entry.getValue().fileName(); }
            });
        }
        return multipart;
    }

    /**
     * A read timeout surfaces as a different exception for every {@link ClientHttpRequestFactory} Spring may pick:
     * {@link SocketTimeoutException} for the Apache and simple clients, {@link HttpTimeoutException} for the JDK
     * client and Netty's own {@code ReadTimeoutException} for the Reactor client. They all mean the same thing to
     * a caller, so they are all reported as {@link TimeoutException}.
     */
    static boolean isTimeout(Throwable throwable) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException
                    || cause instanceof HttpTimeoutException
                    || isNamedLikeATimeout(cause)) {
                return true;
            }
            if (cause.getCause() == cause) {
                break;
            }
        }
        return false;
    }

    /**
     * Clients that are optional dependencies cannot be referenced by type, so their timeouts are recognised by
     * name. Matching the simple name rather than the fully qualified one keeps this working when the class has
     * been relocated, which is common for Netty in shaded distributions.
     */
    private static boolean isNamedLikeATimeout(Throwable cause) {
        return cause.getClass().getSimpleName().contains("Timeout");
    }


    /**
     * Not every {@link ClientHttpRequestFactory} makes the error body of a failed response available: the simple
     * factory, backed by {@link java.net.HttpURLConnection}, discards it on a 401, because it cannot replay a
     * streamed request body to retry the request with credentials. When the body is there it is the most useful
     * thing a caller can be given, and when it is not, anything is better than an exception with no message.
     */
    static String errorMessage(String body, String fallback) {
        return isNullOrBlank(body) ? fallback : body;
    }

}
