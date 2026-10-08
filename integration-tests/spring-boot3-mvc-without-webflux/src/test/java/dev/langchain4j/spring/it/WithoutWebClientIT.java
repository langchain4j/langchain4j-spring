package dev.langchain4j.spring.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import dev.langchain4j.exception.AsyncNotSupportedException;
import dev.langchain4j.http.client.spring.restclient.WebClientBuilderHolder;
import org.springframework.util.ClassUtils;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The starters declare {@code spring-webflux} as an optional dependency, so their own tests always run with it. This
 * application does not have it, as most applications do not.
 */
class WithoutWebClientIT {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void stubOpenAi() {
        OpenAiStubs.stub(wireMock);
    }

    private static ConfigurableApplicationContext start(SpringApplicationBuilder application, String... properties) {
        return application
                .properties("server.port=0")
                .properties(OpenAiStubs.properties(wireMock))
                .properties(properties)
                .run();
    }

    @Test
    void should_not_have_webclient_on_the_classpath() {
        // guards the purpose of this module: if WebClient ever arrives transitively, these tests prove nothing
        assertThat(ClassUtils.isPresent("org.springframework.web.reactive.function.client.WebClient", null)).isFalse();
    }

    @Test
    void should_start_and_serve_blocking_and_token_stream_calls() {
        SpringApplicationBuilder application = new SpringApplicationBuilder(TestApplication.class);
        try (ConfigurableApplicationContext context = start(application)) {

            assertThat(application.application().getWebApplicationType()).isEqualTo(WebApplicationType.SERVLET);
            assertThat(context.getBeanNamesForType(WebClientBuilderHolder.class)).isEmpty();

            Assistant assistant = context.getBean(Assistant.class);
            assertThat(assistant.chat("What is the capital of Germany?")).isEqualTo("Berlin");
            assertThat(assistant.chatFlux("What is the capital of Germany?").collectList().block(Duration.ofSeconds(10)))
                    .containsExactly("Ber", "lin");
        }
    }

    @Test
    void should_fail_non_blocking_calls_with_a_message_saying_what_to_add() {
        try (ConfigurableApplicationContext context = start(new SpringApplicationBuilder(TestApplication.class))) {

            Assistant assistant = context.getBean(Assistant.class);

            assertThatThrownBy(() -> assistant.chatAsync("What is the capital of Germany?").get(10, SECONDS))
                    .rootCause()
                    .isExactlyInstanceOf(AsyncNotSupportedException.class)
                    .hasMessageContaining("spring-webflux");
            assertThatThrownBy(() -> assistant.chatMono("What is the capital of Germany?").block(Duration.ofSeconds(10)))
                    .satisfies(e -> assertThat(rootCause(e))
                            .isExactlyInstanceOf(AsyncNotSupportedException.class)
                            .hasMessageContaining("spring-webflux"));
            assertThatThrownBy(() -> assistant.chatEvents("What is the capital of Germany?").blockLast(Duration.ofSeconds(10)))
                    .satisfies(e -> assertThat(rootCause(e))
                            .isExactlyInstanceOf(AsyncNotSupportedException.class)
                            .hasMessageContaining("spring-webflux"));
        }
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
