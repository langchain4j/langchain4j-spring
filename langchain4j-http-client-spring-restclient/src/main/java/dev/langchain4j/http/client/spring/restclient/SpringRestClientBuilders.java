package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorSettings;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Creates the {@link SpringRestClientBuilder} of the LangChain4j Spring Boot starters, configured the way Spring Boot
 * configures its own HTTP clients. Not meant to be used by applications.
 */
@Internal
public final class SpringRestClientBuilders {

    private static final Logger log = LoggerFactory.getLogger(SpringRestClientBuilders.class);

    private SpringRestClientBuilders() {
    }

    /**
     * Creates a {@link SpringRestClientBuilder} from the beans of the application:
     * <ul>
     * <li>its {@link RestClient.Builder}, or a new one;</li>
     * <li>its {@link WebClientBuilderHolder}, or else its {@code WebClient.Builder}, or a new one, when
     * {@code spring-webflux} is on the classpath;</li>
     * <li>the {@link ClientHttpRequestFactoryBuilder}, {@link ClientHttpRequestFactorySettings},
     * {@link ClientHttpConnectorBuilder} and {@link ClientHttpConnectorSettings} that Spring Boot configures.</li>
     * </ul>
     * When there are several beans of one of these types and none of them is primary, none of them is used, and a
     * warning is logged.
     */
    public static SpringRestClientBuilder fromSpringBoot(ListableBeanFactory beanFactory) {
        RestClient.Builder restClientBuilder = unique(beanFactory, RestClient.Builder.class);
        SpringRestClientBuilder builder = SpringRestClient.builder()
                .restClientBuilder(restClientBuilder == null ? RestClient.builder() : restClientBuilder)
                .clientHttpRequestFactoryBuilder(unique(beanFactory, ClientHttpRequestFactoryBuilder.class))
                .clientHttpRequestFactorySettings(unique(beanFactory, ClientHttpRequestFactorySettings.class))
                .clientHttpConnectorBuilder(unique(beanFactory, ClientHttpConnectorBuilder.class))
                .clientHttpConnectorSettings(unique(beanFactory, ClientHttpConnectorSettings.class));
        if (SpringRestClient.WEBFLUX_PRESENT) {
            builder.webClientBuilder(WebFlux.webClientBuilder(beanFactory));
        }
        return builder;
    }

    private static <T> T unique(ListableBeanFactory beanFactory, Class<T> type) {
        T bean = beanFactory.getBeanProvider(type).getIfUnique();
        if (bean == null) {
            String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type, true, false);
            if (beanNames.length > 1) {
                log.warn("Found {} beans of type {} ({}) and none of them is primary, so LangChain4j uses none of"
                                + " them for its HTTP clients. Mark one of them as @Primary.",
                        beanNames.length, type.getName(), String.join(", ", beanNames));
            }
        }
        return bean;
    }

    /**
     * Keeps the references to {@code spring-webflux}, an optional dependency, out of the enclosing class.
     */
    private static final class WebFlux {

        static WebClientBuilderHolder webClientBuilder(ListableBeanFactory beanFactory) {
            WebClientBuilderHolder holder = unique(beanFactory, WebClientBuilderHolder.class);
            if (holder != null) {
                return holder;
            }
            WebClient.Builder webClientBuilder = unique(beanFactory, WebClient.Builder.class);
            return WebClientBuilderHolder.of(webClientBuilder == null ? WebClient.builder() : webClientBuilder);
        }
    }
}
