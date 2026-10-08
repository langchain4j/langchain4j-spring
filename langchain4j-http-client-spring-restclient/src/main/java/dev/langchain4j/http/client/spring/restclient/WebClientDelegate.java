package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.FormDataFile;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.HttpResponseReceived;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorSettings;
import org.springframework.boot.http.client.reactive.HttpComponentsClientHttpConnectorBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static dev.langchain4j.internal.Utils.getOrDefault;

/**
 * The non-blocking half of {@link SpringRestClient}, backed by Spring's {@link WebClient}.
 * <p>
 * {@code spring-webflux} is an optional dependency, so every reference to it is kept in this class: it is only
 * loaded when {@link SpringRestClient} has found {@link WebClient} on the classpath.
 */
class WebClientDelegate {

    private final WebClient.Builder webClientBuilder;
    private final ClientHttpConnectorBuilder<?> connectorBuilder;
    private final ClientHttpConnectorSettings settings;

    private volatile WebClient webClient;

    WebClientDelegate(SpringRestClientBuilder builder) {

        ClientHttpConnectorSettings settings = ClientHttpConnectorSettings.defaults();
        if (builder.connectTimeout() != null) {
            settings = settings.withConnectTimeout(builder.connectTimeout());
        }
        if (builder.readTimeout() != null) {
            settings = settings.withReadTimeout(builder.readTimeout());
        }
        this.settings = settings;

        ClientHttpConnectorBuilder<?> connectorBuilder = getOrDefault(
                builder.clientHttpConnectorBuilder(), ClientHttpConnectorBuilder::detect);
        if (connectorBuilder instanceof HttpComponentsClientHttpConnectorBuilder httpComponentsBuilder) {
            connectorBuilder = httpComponentsBuilder.withHttpClientCustomizer(HttpAsyncClientBuilder::disableAutomaticRetries);
        }
        this.connectorBuilder = connectorBuilder;

        this.webClientBuilder = builder.webClientBuilder() == null
                ? WebClient.builder()
                : builder.webClientBuilder().webClientBuilder().clone();
    }

    /**
     * Builds the {@link WebClient} on first use rather than in the constructor: depending on the connector, building
     * it can start I/O threads, and a client that only ever makes blocking calls should not pay for them.
     */
    private WebClient webClient() {
        WebClient result = webClient;
        if (result == null) {
            synchronized (this) {
                result = webClient;
                if (result == null) {
                    result = webClientBuilder
                            .clientConnector(connectorBuilder.build(settings))
                            .build();
                    webClient = result;
                }
            }
        }
        return result;
    }

    CompletableFuture<SuccessfulHttpResponse> executeAsync(HttpRequest request) {
        return toWebClientRequest(request)
                .exchangeToMono(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        return toHttpException(response);
                    }
                    return readBody(response)
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .map(body -> toSuccessfulHttpResponse(response, body.orElse(null)));
                })
                .onErrorMap(SpringRestClient::isTimeout, TimeoutException::new)
                .toFuture();
    }

    Flow.Publisher<HttpStreamingEvent> stream(HttpRequest request, ServerSentEventParser parser) {
        Flux<HttpStreamingEvent> events = toWebClientRequest(request)
                .<HttpStreamingEvent>exchangeToFlux(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        return WebClientDelegate.<HttpStreamingEvent>toHttpException(response).flux();
                    }
                    ServerSentEventParser.Incremental incremental = parser.incremental();
                    Flux<ServerSentEvent> serverSentEvents = response.bodyToFlux(DataBuffer.class)
                            .concatMapIterable(buffer -> feed(incremental, buffer))
                            .concatWith(Flux.defer(() -> Flux.fromIterable(incremental.flush())));
                    return Flux.<HttpStreamingEvent>just(new HttpResponseReceived(toSuccessfulHttpResponse(response, null)))
                            .concatWith(serverSentEvents);
                })
                .onErrorMap(SpringRestClient::isTimeout, TimeoutException::new);
        return JdkFlowAdapter.publisherToFlowPublisher(events);
    }

    private static List<ServerSentEvent> feed(ServerSentEventParser.Incremental incremental, DataBuffer buffer) {
        try (DataBuffer.ByteBufferIterator byteBuffers = buffer.readableByteBuffers()) {
            List<ServerSentEvent> events = new ArrayList<>();
            while (byteBuffers.hasNext()) {
                ByteBuffer byteBuffer = byteBuffers.next();
                events.addAll(incremental.feed(byteBuffer));
            }
            return events;
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    private WebClient.RequestHeadersSpec<?> toWebClientRequest(HttpRequest request) {
        WebClient.RequestBodySpec requestBodySpec = webClient()
                .method(org.springframework.http.HttpMethod.valueOf(request.method().name()))
                .uri(request.url())
                .headers(httpHeaders -> httpHeaders.putAll(request.headers()));

        if (request.formDataFields().isEmpty() && request.formDataFiles().isEmpty()) {
            if (request.body() != null) {
                return requestBodySpec.bodyValue(request.body());
            }
            return requestBodySpec;
        }
        return requestBodySpec.body(BodyInserters.fromMultipartData(
                toMultiValueMap(request.formDataFields(), request.formDataFiles())));
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

    private static <T> Mono<T> toHttpException(ClientResponse response) {
        return readBody(response)
                .map(body -> new String(body, StandardCharsets.UTF_8))
                .defaultIfEmpty("")
                .flatMap(body -> Mono.error(new HttpException(
                        response.statusCode().value(),
                        SpringRestClient.errorMessage(body, response.statusCode().toString()))));
    }

    /**
     * Reads the whole body without the in-memory limit of WebClient's codecs (256 KB by default), so that a large
     * response, such as a batch of embeddings or a base64-encoded image, is handled as on the blocking path.
     */
    private static Mono<byte[]> readBody(ClientResponse response) {
        return DataBufferUtils.join(response.body(BodyExtractors.toDataBuffers()))
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return bytes;
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                });
    }

    private static SuccessfulHttpResponse toSuccessfulHttpResponse(ClientResponse response, byte[] body) {
        return SuccessfulHttpResponse.builder()
                .statusCode(response.statusCode().value())
                .headers(response.headers().asHttpHeaders())
                .body(body)
                .build();
    }
}
