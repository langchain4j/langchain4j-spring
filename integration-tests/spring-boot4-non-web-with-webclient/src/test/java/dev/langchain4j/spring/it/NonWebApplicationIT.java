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

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * An application without a web layer (no spring-boot-starter-web), set up for the non-blocking modes as the
 * Spring Boot integration documentation says.
 */
class NonWebApplicationIT {

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
    void should_stay_a_non_web_application_and_serve_non_blocking_calls() throws Exception {
        SpringApplicationBuilder application = new SpringApplicationBuilder(TestApplication.class);
        try (ConfigurableApplicationContext context = start(application)) {

            assertThat(application.application().getWebApplicationType()).isEqualTo(WebApplicationType.NONE);

            assertNonBlockingCallsWork(context.getBean(Assistant.class));
            assertNonBlockingRequestsWereBuiltFromTheApplicationWebClientBuilder();
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
