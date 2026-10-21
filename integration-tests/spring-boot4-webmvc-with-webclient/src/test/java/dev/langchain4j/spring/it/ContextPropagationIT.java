package dev.langchain4j.spring.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.MDC;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * With langchain4j.executor.use-spring-task-executor=true, the work LangChain4j moves off the calling thread runs on
 * the application's task executor, so context that the executor propagates (here MDC, through Micrometer context
 * propagation) reaches a blocking tool called during a non-blocking AI Service call.
 */
class ContextPropagationIT {

    private static final String USER_MESSAGE = "Use the tool to find the capital of Germany";

    private static final String TOOL_CALL = """
            {
              "id": "chatcmpl-tool",
              "created": 1700000000,
              "model": "gpt-4o-mini",
              "choices": [{
                "index": 0,
                "message": {
                  "role": "assistant",
                  "content": null,
                  "tool_calls": [{"id": "call-1", "type": "function", "function": {"name": "capitalOfGermany", "arguments": "{}"}}]
                },
                "finish_reason": "tool_calls"
              }],
              "usage": {"prompt_tokens": 10, "completion_tokens": 1, "total_tokens": 11}
            }
            """;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    static void propagateMdc() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new Slf4jThreadLocalAccessor());
    }

    @AfterAll
    static void stopPropagatingMdc() {
        ContextRegistry.getInstance().removeThreadLocalAccessor(Slf4jThreadLocalAccessor.KEY);
    }

    @BeforeEach
    void stubOpenAi() {
        OpenAiStubs.stub(wireMock);
        // first request: the model asks for the tool; second request (with the tool result): the final answer
        wireMock.stubFor(post(urlEqualTo(OpenAiStubs.CHAT_COMPLETIONS_PATH))
                .withRequestBody(containing(USER_MESSAGE))
                .atPriority(1)
                .willReturn(okJson(TOOL_CALL)));
        wireMock.stubFor(post(urlEqualTo(OpenAiStubs.CHAT_COMPLETIONS_PATH))
                .withRequestBody(matchingJsonPath("$.messages[?(@.role == 'tool')]"))
                .atPriority(0)
                .willReturn(okJson(OpenAiStubs.chatCompletion("Berlin"))));
    }

    @Test
    void should_propagate_context_to_tools_when_using_the_spring_task_executor() throws Exception {
        try (ConfigurableApplicationContext context = start("langchain4j.executor.use-spring-task-executor=true")) {

            String answer = callWithRequestId(context.getBean(Assistant.class), "request-42");

            assertThat(answer).isEqualTo("Berlin");
            assertThat(context.getBean(CapitalTool.class).requestIdsSeen).containsExactly("request-42");
        }
    }

    @Test
    void should_not_propagate_context_to_tools_with_the_default_executor() throws Exception {
        try (ConfigurableApplicationContext context = start()) {

            String answer = callWithRequestId(context.getBean(Assistant.class), "request-42");

            assertThat(answer).isEqualTo("Berlin");
            assertThat(context.getBean(CapitalTool.class).requestIdsSeen).containsExactly("null");
        }
    }

    private static String callWithRequestId(Assistant assistant, String requestId) throws Exception {
        MDC.put(CapitalTool.MDC_KEY, requestId);
        try {
            return assistant.chatAsync(USER_MESSAGE).get(10, SECONDS);
        } finally {
            MDC.remove(CapitalTool.MDC_KEY);
        }
    }

    private static ConfigurableApplicationContext start(String... properties) {
        return new SpringApplicationBuilder(TestApplication.class, ContextPropagationConfiguration.class)
                .properties("server.port=0")
                .properties(OpenAiStubs.properties(wireMock))
                .properties(properties)
                .run();
    }

    @Configuration(proxyBeanMethods = false)
    static class ContextPropagationConfiguration {

        @Bean
        ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
            return new ContextPropagatingTaskDecorator();
        }
    }
}
