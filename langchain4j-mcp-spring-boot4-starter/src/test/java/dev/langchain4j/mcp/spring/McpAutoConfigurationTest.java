package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolProvider;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class McpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(McpAutoConfiguration.class));

    @Test
    void does_not_create_clients_or_tool_provider_without_client_configuration() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(McpClient.class);
            assertThat(context).doesNotHaveBean(ToolProvider.class);
        });
    }

    @Test
    void creates_a_named_http_client_and_tool_provider_from_configuration() throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer()) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url(),
                            "langchain4j.mcp.clients.files.headers.X-Environment=internal"
                    )
                    .run(context -> {
                        assertThat(context.getBean("mcpClient.files")).isInstanceOf(DefaultMcpClient.class);
                        assertThat(context.getBeansOfType(McpClient.class)).containsOnlyKeys("mcpClient.files");
                        assertThat(context.getBean(ToolProvider.class)).isInstanceOf(McpToolProvider.class);
                        assertThat(server.environmentHeader()).isEqualTo("internal");
                    });
        }
    }

    @Test
    void backs_off_when_the_application_provides_its_own_tool_provider() throws IOException {
        ToolProvider customToolProvider = mock(ToolProvider.class);
        try (McpProtocolTestServer server = new McpProtocolTestServer()) {
            contextRunner
                    .withBean("customToolProvider", ToolProvider.class, () -> customToolProvider)
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> {
                        assertThat(context.getBean(ToolProvider.class)).isSameAs(customToolProvider);
                        assertThat(context).doesNotHaveBean(McpToolProvider.class);
                    });
        }
    }

    @Test
    void can_disable_mcp_autoconfiguration() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mcp.enabled=false",
                        "langchain4j.mcp.clients.files.transport=STDIO",
                        "langchain4j.mcp.clients.files.command[0]=unused"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(McpClient.class);
                    assertThat(context).doesNotHaveBean(ToolProvider.class);
                });
    }

    @Test
    void reports_missing_transport_configuration() {
        contextRunner
                .withPropertyValues("langchain4j.mcp.clients.incomplete.transport=HTTP")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("HTTP MCP client 'incomplete' must define a URL");
                });
    }

    private static final class McpProtocolTestServer implements AutoCloseable {

        private static final Pattern REQUEST_ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

        private final HttpServer server;
        private final AtomicReference<String> environmentHeader = new AtomicReference<>();

        private McpProtocolTestServer() throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/mcp", exchange -> {
                String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Matcher matcher = REQUEST_ID.matcher(request);
                if (!matcher.find()) {
                    exchange.sendResponseHeaders(400, -1);
                    exchange.close();
                    return;
                }
                environmentHeader.set(exchange.getRequestHeaders().getFirst("X-Environment"));
                byte[] response = ("{\"jsonrpc\":\"2.0\",\"id\":" + matcher.group(1)
                        + ",\"result\":{\"supportedVersions\":[\"2026-07-28\"],\"capabilities\":{}}}")
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();
        }

        private String url() {
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/mcp";
        }

        private String environmentHeader() {
            return environmentHeader.get();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
