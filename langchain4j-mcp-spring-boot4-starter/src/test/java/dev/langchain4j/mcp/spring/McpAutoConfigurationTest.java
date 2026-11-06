package dev.langchain4j.mcp.spring;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.mock;

class McpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(McpAutoConfiguration.class));

    @Test
    void should_not_create_clients_or_tool_provider_when_no_clients_are_configured() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(McpClient.class);
            assertThat(context).doesNotHaveBean(ToolProvider.class);
        });
    }

    @Test
    void should_create_named_streamable_http_client_and_tool_provider() throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url(),
                            "langchain4j.mcp.clients.files.custom-headers.X-Environment=internal"
                    )
                    .run(context -> {
                        assertThat(context.getBean("mcpClient.files")).isInstanceOf(DefaultMcpClient.class);
                        assertThat(context.getBeansOfType(McpClient.class)).containsOnlyKeys("mcpClient.files");
                        assertThat(context.getBean(McpClient.class).key()).isEqualTo("files");
                        assertThat(context.getBean(ToolProvider.class)).isInstanceOf(McpToolProvider.class);
                        assertThat(server.environmentHeader()).isEqualTo("internal");
                    });
        }
    }

    @Test
    void should_provide_tools_of_all_configured_clients() throws IOException {
        try (McpProtocolTestServer files = new McpProtocolTestServer("read_file");
             McpProtocolTestServer search = new McpProtocolTestServer("search")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=streamable-http",
                            "langchain4j.mcp.clients.files.url=" + files.url(),
                            "langchain4j.mcp.clients.search.transport=streamable-http",
                            "langchain4j.mcp.clients.search.url=" + search.url()
                    )
                    .run(context -> {
                        assertThat(context.getBeansOfType(McpClient.class))
                                .containsOnlyKeys("mcpClient.files", "mcpClient.search");
                        assertThat(toolNames(context.getBean(ToolProvider.class)))
                                .containsExactlyInAnyOrder("read_file", "search");
                    });
        }
    }

    @Test
    void should_start_stdio_client_with_configured_command_and_environment() throws URISyntaxException {
        contextRunner
                .withPropertyValues(stdioTestServerCommand("local"))
                .withPropertyValues(
                        "langchain4j.mcp.clients.local.transport=STDIO",
                        "langchain4j.mcp.clients.local.environment.MCP_TEST_VALUE=value from Spring configuration"
                )
                .run(context -> {
                    assertThat(context.getBean(McpClient.class).key()).isEqualTo("local");
                    assertThat(context.getBean(McpClient.class).listTools())
                            .extracting(ToolSpecification::name, ToolSpecification::description)
                            .containsExactly(tuple("echo_environment", "value from Spring configuration"));
                });
    }

    @Test
    void should_stop_stdio_server_when_client_initialization_fails() throws Exception {
        contextRunner
                .withPropertyValues(stdioTestServerCommand("failing"))
                .withPropertyValues(
                        "langchain4j.mcp.clients.failing.transport=STDIO",
                        "langchain4j.mcp.clients.failing.command[4]=fail"
                )
                .run(context -> assertThat(context).hasFailed());

        for (ProcessHandle process : stdioTestServerProcesses()) {
            process.onExit().get(10, SECONDS);
        }
        assertThat(stdioTestServerProcesses()).isEmpty();
    }

    @Test
    void should_close_clients_when_context_is_closed() throws IOException {
        AtomicReference<McpClient> client = new AtomicReference<>();
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> client.set(context.getBean(McpClient.class)));
        }

        assertThatThrownBy(() -> client.get().checkHealth())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The client is closed");
    }

    @Test
    void should_back_off_when_application_provides_its_own_tool_provider() throws IOException {
        ToolProvider customToolProvider = mock(ToolProvider.class);
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withBean("customToolProvider", ToolProvider.class, () -> customToolProvider)
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> {
                        assertThat(context.getBean(ToolProvider.class)).isSameAs(customToolProvider);
                        assertThat(context).doesNotHaveBean(McpToolProvider.class);
                        assertThat(context).hasBean("mcpClient.files");
                    });
        }
    }

    @Test
    void should_keep_application_bean_with_the_same_name_as_a_configured_client() {
        McpClient applicationClient = mock(McpClient.class);
        contextRunner
                .withBean("mcpClient.files", McpClient.class, () -> applicationClient)
                .withPropertyValues(
                        "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                        "langchain4j.mcp.clients.files.url=http://localhost:1/mcp"
                )
                .run(context -> assertThat(context.getBean("mcpClient.files")).isSameAs(applicationClient));
    }

    @Test
    void should_not_create_clients_or_tool_provider_when_disabled() {
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
    void should_fail_when_client_has_no_transport() {
        assertStartupFailure(
                "MCP client 'incomplete' has no transport. "
                        + "Set 'langchain4j.mcp.clients.incomplete.transport' to STREAMABLE_HTTP or STDIO",
                "langchain4j.mcp.clients.incomplete.url=http://localhost:1/mcp");
    }

    @Test
    void should_fail_when_streamable_http_client_has_no_url() {
        assertStartupFailure(
                "MCP client 'incomplete' has no URL. "
                        + "Set 'langchain4j.mcp.clients.incomplete.url' to the URL of the MCP server endpoint",
                "langchain4j.mcp.clients.incomplete.transport=STREAMABLE_HTTP");
    }

    @Test
    void should_fail_when_stdio_client_has_no_command() {
        assertStartupFailure(
                "MCP client 'incomplete' has no command. Set 'langchain4j.mcp.clients.incomplete.command' "
                        + "to the program that starts the MCP server and its arguments",
                "langchain4j.mcp.clients.incomplete.transport=STDIO");
    }

    @Test
    void should_fail_when_stdio_client_sets_streamable_http_properties() {
        assertStartupFailure(
                "MCP client 'mixed' uses the STDIO transport, "
                        + "but 'url' and 'custom-headers' apply only to the STREAMABLE_HTTP transport",
                "langchain4j.mcp.clients.mixed.transport=STDIO",
                "langchain4j.mcp.clients.mixed.command[0]=unused",
                "langchain4j.mcp.clients.mixed.custom-headers.Authorization=Bearer token");
    }

    @Test
    void should_fail_when_streamable_http_client_sets_stdio_properties() {
        assertStartupFailure(
                "MCP client 'mixed' uses the STREAMABLE_HTTP transport, "
                        + "but 'command' and 'environment' apply only to the STDIO transport",
                "langchain4j.mcp.clients.mixed.transport=STREAMABLE_HTTP",
                "langchain4j.mcp.clients.mixed.url=http://localhost:1/mcp",
                "langchain4j.mcp.clients.mixed.environment.API_KEY=secret");
    }

    private void assertStartupFailure(String expectedMessage, String... properties) {
        contextRunner
                .withPropertyValues(properties)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining(expectedMessage);
                });
    }

    private static String[] stdioTestServerCommand(String clientName) throws URISyntaxException {
        String java = ProcessHandle.current().info().command().orElseThrow();
        String classpath = Path.of(StdioTestMcpServer.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        String prefix = "langchain4j.mcp.clients." + clientName + ".command";
        return new String[] {
                prefix + "[0]=" + java,
                prefix + "[1]=-cp",
                prefix + "[2]=" + classpath,
                prefix + "[3]=" + StdioTestMcpServer.class.getName()
        };
    }

    private static List<ProcessHandle> stdioTestServerProcesses() {
        return ProcessHandle.current().children()
                .filter(ProcessHandle::isAlive)
                .filter(process -> process.info().commandLine()
                        .map(commandLine -> commandLine.contains(StdioTestMcpServer.class.getName()))
                        .orElse(false))
                .toList();
    }

    private static Set<String> toolNames(ToolProvider toolProvider) {
        return toolProvider.provideTools(new ToolProviderRequest("default", UserMessage.from("hi")))
                .tools().keySet().stream()
                .map(ToolSpecification::name)
                .collect(toSet());
    }

    private static final class McpProtocolTestServer implements AutoCloseable {

        private static final Pattern REQUEST_ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]+)\"");

        private final HttpServer server;
        private final AtomicReference<String> environmentHeader = new AtomicReference<>();

        private McpProtocolTestServer(String toolName) throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/mcp", exchange -> {
                String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Matcher id = REQUEST_ID.matcher(request);
                if (!id.find()) {
                    exchange.sendResponseHeaders(202, -1);
                    exchange.close();
                    return;
                }
                environmentHeader.set(exchange.getRequestHeaders().getFirst("X-Environment"));
                Matcher method = METHOD.matcher(request);
                String result = method.find() && method.group(1).equals("tools/list")
                        ? "{\"tools\":[{\"name\":\"" + toolName + "\",\"inputSchema\":{\"type\":\"object\",\"properties\":{}}}]}"
                        : "{\"supportedVersions\":[\"2026-07-28\"],\"capabilities\":{\"tools\":{}}}";
                byte[] response = ("{\"jsonrpc\":\"2.0\",\"id\":" + id.group(1) + ",\"result\":" + result + "}")
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
