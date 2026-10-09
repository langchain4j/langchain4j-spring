package dev.langchain4j.spring.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import dev.langchain4j.service.AiServiceStreamingEvent;
import dev.langchain4j.service.AiServiceStreamingEvent.FinalResponseEvent;
import dev.langchain4j.service.AiServiceStreamingEvent.PartialResponseEvent;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.http.client.spring.restclient.WebClientBuilderHolder;
import org.springframework.boot.http.client.reactive.JdkClientHttpConnectorBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class WithWebClientIT {

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
    void should_stay_a_servlet_application_and_serve_every_kind_of_call() throws Exception {
        SpringApplicationBuilder application = new SpringApplicationBuilder(TestApplication.class);
        try (ConfigurableApplicationContext context = start(application)) {

            assertThat(application.application().getWebApplicationType()).isEqualTo(WebApplicationType.SERVLET);

            Assistant assistant = context.getBean(Assistant.class);
            assertThat(assistant.chat("What is the capital of Germany?")).isEqualTo("Berlin");
            assertThat(assistant.chatFlux("What is the capital of Germany?").collectList().block(Duration.ofSeconds(10)))
                    .containsExactly("Ber", "lin");
            assertNonBlockingCallsWork(assistant);
        }
    }

    @Test
    void should_build_non_blocking_requests_from_the_application_web_client_builder() throws Exception {
        try (ConfigurableApplicationContext context = start(new SpringApplicationBuilder(TestApplication.class))) {

            assertThat(context.getBean(WebClientBuilderHolder.class).webClientBuilder())
                    .isInstanceOf(WebClient.Builder.class);

            assertNonBlockingCallsWork(context.getBean(Assistant.class));
            assertNonBlockingRequestsWereBuiltFromTheApplicationWebClientBuilder();
        }
    }

    @Test
    void should_use_the_connector_configured_in_spring_boot() {
        try (ConfigurableApplicationContext context =
                     start(new SpringApplicationBuilder(TestApplication.class), "spring.http.reactiveclient.connector=jdk")) {

            assertThat(context.getBeansOfType(HttpClientBuilder.class).values())
                    .isNotEmpty()
                    .allSatisfy(httpClientBuilder ->
                            assertThat(((SpringRestClientBuilder) httpClientBuilder).clientHttpConnectorBuilder())
                                    .isInstanceOf(JdkClientHttpConnectorBuilder.class));
        }
    }

    private static void assertNonBlockingCallsWork(Assistant assistant) throws Exception {
        assertThat(assistant.chatAsync("What is the capital of Germany?").get(10, SECONDS)).isEqualTo("Berlin");
        assertThat(assistant.chatMono("What is the capital of Germany?").block(Duration.ofSeconds(10))).isEqualTo("Berlin");

        List<AiServiceStreamingEvent> events =
                assistant.chatEvents("What is the capital of Germany?").collectList().block(Duration.ofSeconds(10));
        assertThat(events)
                .filteredOn(PartialResponseEvent.class::isInstance)
                .extracting(event -> ((PartialResponseEvent) event).partialResponse().text())
                .containsExactly("Ber", "lin");
        assertThat(events.get(events.size() - 1)).isInstanceOfSatisfying(FinalResponseEvent.class,
                event -> assertThat(event.chatResponse().aiMessage().text()).isEqualTo("Berlin"));
    }

    private static void assertNonBlockingRequestsWereBuiltFromTheApplicationWebClientBuilder() {
        // the header is set by a WebClientCustomizer of the application, so only requests sent with the
        // WebClient.Builder that Spring Boot auto-configures carry it
        wireMock.verify(postRequestedFor(urlEqualTo(OpenAiStubs.CHAT_COMPLETIONS_PATH))
                .withHeader(TestApplication.CUSTOMIZER_HEADER, equalTo("test-application")));
    }
}
