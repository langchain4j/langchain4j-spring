package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.FormDataFile;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventContext;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import dev.langchain4j.http.client.sse.ServerSentEventParsingHandle;
import dev.langchain4j.http.client.sse.StreamingHttpEvent;
import mutiny.zero.BackpressureStrategy;
import mutiny.zero.TubeConfiguration;
import mutiny.zero.ZeroPublisher;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.http.client.sse.ServerSentEventListenerUtils.ignoringExceptions;
import static dev.langchain4j.internal.Utils.getOrDefault;

public class SpringRestClient implements HttpClient {

    private final RestClient delegate;
    private final AsyncTaskExecutor asyncRequestExecutor;
    private final AsyncTaskExecutor streamingRequestExecutor;

    public SpringRestClient(SpringRestClientBuilder builder) {

        RestClient.Builder restClientBuilder = getOrDefault(builder.restClientBuilder(), RestClient::builder);

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults();
        if (builder.connectTimeout() != null) {
            settings = settings.withConnectTimeout(builder.connectTimeout());
        }
        if (builder.readTimeout() != null) {
            settings = settings.withReadTimeout(builder.readTimeout());
        }
        ClientHttpRequestFactory clientHttpRequestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);

        this.delegate = restClientBuilder
                .requestFactory(clientHttpRequestFactory)
                .build();

        this.asyncRequestExecutor = getOrDefault(builder.asyncRequestExecutor(), () -> {
            if (builder.createDefaultAsyncRequestExecutor()) {
                return createDefaultRequestExecutor(); // TODO create lazily when it is first used?
            } else {
                return null;
            }
        });

        this.streamingRequestExecutor = getOrDefault(builder.streamingRequestExecutor(), () -> {
            if (builder.createDefaultStreamingRequestExecutor()) {
                return createDefaultRequestExecutor(); // TODO create lazily when it is first used?
            } else {
                return null;
            }
        });
    }

    private static AsyncTaskExecutor createDefaultRequestExecutor() {
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
            ResponseEntity<String> responseEntity = toSpringRestClientRequest(request)
                    .retrieve()
                    .toEntity(String.class);

            return SuccessfulHttpResponse.builder()
                    .statusCode(responseEntity.getStatusCode().value())
                    .headers(responseEntity.getHeaders())
                    .body(responseEntity.getBody())
                    .build();
        } catch (RestClientResponseException e) {
            throw new HttpException(e.getStatusCode().value(), e.getMessage());
        } catch (Exception e) {
            if (e.getCause() instanceof SocketTimeoutException) {
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

                                HttpException exception = new HttpException(statusCode, body);
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
                if (e.getCause() instanceof SocketTimeoutException) {
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
     * Spring's {@link RestClient} is blocking, so this offloads the blocking {@link #execute(HttpRequest)}
     * to the async request executor and completes the returned future on that thread — the calling
     * thread is never blocked, but a worker thread performs the request.
     */
    @Override
    public CompletableFuture<SuccessfulHttpResponse> executeAsync(HttpRequest request) {
        return asyncRequestExecutor.submitCompletable(() -> execute(request)); // TODO
    }

    /**
     * {@inheritDoc}
     * <p>
     * Backed by the blocking streaming path, so a worker thread (from the streaming request executor) is
     * pinned for the lifetime of the stream. Cancelling the subscription cancels the SSE parsing, which
     * closes the stream and frees the thread.
     */
    @Override
    public Flow.Publisher<StreamingHttpEvent> executeWithPublisher(HttpRequest request, ServerSentEventParser parser) {
        TubeConfiguration config = new TubeConfiguration()
                .withBackpressureStrategy(BackpressureStrategy.BUFFER)
                .withBufferSize(256);
        return ZeroPublisher.create(config, tube -> {
            AtomicReference<ServerSentEventParsingHandle> parsingHandle = new AtomicReference<>();
            tube.whenCancelled(() -> {
                ServerSentEventParsingHandle handle = parsingHandle.get();
                if (handle != null) {
                    handle.cancel();
                }
            });
            execute(request, parser, new ServerSentEventListener() {
                @Override
                public void onOpen(SuccessfulHttpResponse response) {
                    if (!tube.cancelled()) {
                        tube.send(response);
                    }
                }

                @Override
                public void onEvent(ServerSentEvent event) {
                    if (!tube.cancelled()) {
                        tube.send(event);
                    }
                }

                @Override
                public void onEvent(ServerSentEvent event, ServerSentEventContext context) {
                    parsingHandle.set(context.parsingHandle());
                    if (!tube.cancelled()) {
                        tube.send(event);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (!tube.cancelled()) {
                        tube.fail(throwable);
                    }
                }

                @Override
                public void onClose() {
                    if (!tube.cancelled()) {
                        tube.complete();
                    }
                }
            });
        });
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
}
