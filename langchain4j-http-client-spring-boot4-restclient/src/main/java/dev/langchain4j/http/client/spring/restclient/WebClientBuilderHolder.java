package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.Experimental;
import org.springframework.web.reactive.function.client.WebClient;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Carries the {@link WebClient.Builder} that {@link SpringRestClient} uses for its non-blocking methods.
 * <p>
 * {@code spring-webflux} is an optional dependency of {@link SpringRestClient}, so {@link SpringRestClientBuilder}
 * cannot mention {@link WebClient} in its methods: applications without {@code spring-webflux} would fail to load
 * it. This type stands in for the builder instead:
 * <pre>{@code
 * SpringRestClient.builder()
 *         .webClientBuilder(WebClientBuilderHolder.of(webClientBuilder))
 *         .build();
 * }</pre>
 * Creating a holder requires {@code spring-webflux} on the classpath.
 */
@Experimental
public final class WebClientBuilderHolder {

    private final WebClient.Builder webClientBuilder;

    private WebClientBuilderHolder(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = ensureNotNull(webClientBuilder, "webClientBuilder");
    }

    /**
     * @param webClientBuilder the builder to use; must not be {@code null}
     */
    public static WebClientBuilderHolder of(WebClient.Builder webClientBuilder) {
        return new WebClientBuilderHolder(webClientBuilder);
    }

    public WebClient.Builder webClientBuilder() {
        return webClientBuilder;
    }
}
