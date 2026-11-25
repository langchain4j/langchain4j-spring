package dev.langchain4j.spring.it;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This application has no spring-webflux, so the MCP starter uses the JDK HTTP client.
 */
class McpIT {

    private static final String MCP_PATH = "/mcp";
    private static final String REDIRECTING_PATH = "/redirecting";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void stubMcpServer() {
        wireMock.stubFor(post(urlEqualTo(MCP_PATH))
                .withRequestBody(matchingJsonPath("$.method", equalTo("tools/list")))
                .atPriority(1)
                .willReturn(result("{\"tools\":[{\"name\":\"read_file\",\"inputSchema\":{\"type\":\"object\",\"properties\":{}}}]}")));
        wireMock.stubFor(post(urlEqualTo(MCP_PATH))
                .withRequestBody(matchingJsonPath("$.id"))
                .atPriority(2)
                .willReturn(result("{\"supportedVersions\":[\"2026-07-28\"],\"protocolVersion\":\"2025-11-25\",\"capabilities\":{\"tools\":{}}}")));
        wireMock.stubFor(post(urlEqualTo(MCP_PATH))
                .atPriority(3)
                .willReturn(aResponse().withStatus(202)));
        wireMock.stubFor(post(urlEqualTo(REDIRECTING_PATH))
                .willReturn(aResponse().withStatus(307).withHeader("Location", wireMock.baseUrl() + MCP_PATH)));
    }

    private static ResponseDefinitionBuilder result(String result) {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"jsonrpc\":\"2.0\",\"id\":{{jsonPath request.body '$.id'}},\"result\":" + result + "}")
                .withTransformers("response-template");
    }

    private static ConfigurableApplicationContext start(String path) {
        return new SpringApplicationBuilder(TestApplication.class)
                .properties("server.port=0")
                .properties(OpenAiStubs.properties(wireMock))
                .properties(
                        "langchain4j.mcp.clients.tools.transport=STREAMABLE_HTTP",
                        "langchain4j.mcp.clients.tools.url=" + wireMock.baseUrl() + path,
                        "langchain4j.mcp.clients.tools.initialization-timeout=5s")
                .run();
    }

    @Test
    void should_use_the_jdk_http_client() {
        try (ConfigurableApplicationContext context = start(MCP_PATH)) {

            McpClient client = context.getBean("mcpClient.tools", McpClient.class);

            assertThat(client.listTools()).extracting(ToolSpecification::name).containsExactly("read_file");
            wireMock.verify(postRequestedFor(urlEqualTo(MCP_PATH)).withHeader("User-Agent", containing("Java-http-client")));
        }
    }

    @Test
    void should_not_follow_redirects() {
        try (ConfigurableApplicationContext context = start(REDIRECTING_PATH)) {

            McpClient client = context.getBean("mcpClient.tools", McpClient.class);

            assertThatThrownBy(client::listTools).isInstanceOf(Exception.class);
            wireMock.verify(0, postRequestedFor(urlEqualTo(MCP_PATH)));
        }
    }
}
