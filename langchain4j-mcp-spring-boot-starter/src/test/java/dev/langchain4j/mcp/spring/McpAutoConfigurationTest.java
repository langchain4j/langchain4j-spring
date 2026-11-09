package dev.langchain4j.mcp.spring;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpClientListener;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class McpAutoConfigurationTest {

    private static final String TOOL_PROVIDER_ENABLED = "langchain4j.mcp.tool-provider.enabled=true";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(McpAutoConfiguration.class));

    @Test
    void should_not_create_clients_or_tool_provider_when_no_clients_are_configured() {
        contextRunner.withPropertyValues(TOOL_PROVIDER_ENABLED).run(context -> {
            assertThat(context).doesNotHaveBean(McpClient.class);
            assertThat(context).doesNotHaveBean(ToolProvider.class);
        });
    }

    @Test
    void should_create_named_client_that_connects_on_first_use() throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url(),
                            "langchain4j.mcp.clients.files.custom-headers.X-Environment=internal"
                    )
                    .run(context -> {
                        assertThat(context.getBeansOfType(McpClient.class)).containsOnlyKeys("mcpClient.files");
                        McpClient client = context.getBean("mcpClient.files", McpClient.class);
                        assertThat(client.key()).isEqualTo("files");
                        assertThat(server.methods()).isEmpty();

                        assertThat(client.listTools()).extracting(ToolSpecification::name).containsExactly("read_file");

                        assertThat(server.methods()).contains("tools/list");
                        assertThat(server.environmentHeader()).isEqualTo("internal");
                    });
        }
    }

    @Test
    void should_start_when_mcp_server_is_unavailable() throws IOException {
        String url = "http://127.0.0.1:" + freePort() + "/mcp";
        contextRunner
                .withPropertyValues(
                        TOOL_PROVIDER_ENABLED,
                        "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                        "langchain4j.mcp.clients.files.url=" + url
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(toolNames(context.getBean(ToolProvider.class))).isEmpty();
                    assertThatThrownBy(() -> context.getBean(McpClient.class).listTools()).isInstanceOf(Exception.class);
                });
    }

    @Test
    void should_connect_on_a_later_call_after_a_failed_connection() throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            server.failing(true);
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> {
                        McpClient client = context.getBean(McpClient.class);
                        assertThatThrownBy(client::listTools).hasMessageContaining("failing on purpose");

                        server.failing(false);

                        assertThat(client.listTools()).extracting(ToolSpecification::name).containsExactly("read_file");
                    });
        }
    }

    @Test
    void should_not_create_tool_provider_unless_enabled() throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> {
                        assertThat(context).hasBean("mcpClient.files");
                        assertThat(context).doesNotHaveBean(ToolProvider.class);
                    });
        }
    }

    @Test
    void should_provide_tools_of_all_configured_clients_when_tool_provider_is_enabled() throws IOException {
        try (McpProtocolTestServer files = new McpProtocolTestServer("read_file");
             McpProtocolTestServer search = new McpProtocolTestServer("search")) {
            contextRunner
                    .withPropertyValues(
                            TOOL_PROVIDER_ENABLED,
                            "langchain4j.mcp.clients.files.transport=streamable-http",
                            "langchain4j.mcp.clients.files.url=" + files.url(),
                            "langchain4j.mcp.clients.search.transport=streamable-http",
                            "langchain4j.mcp.clients.search.url=" + search.url()
                    )
                    .run(context -> {
                        assertThat(context.getBean(ToolProvider.class)).isInstanceOf(McpToolProvider.class);
                        assertThat(files.methods()).isEmpty();
                        assertThat(toolNames(context.getBean(ToolProvider.class)))
                                .containsExactlyInAnyOrder("read_file", "search");
                    });
        }
    }

    @Test
    void should_start_stdio_server_on_first_use_with_configured_command_and_environment() throws URISyntaxException {
        contextRunner
                .withPropertyValues(stdioTestServerCommand("local"))
                .withPropertyValues(
                        "langchain4j.mcp.clients.local.transport=STDIO",
                        "langchain4j.mcp.clients.local.environment.MCP_TEST_VALUE=value from Spring configuration"
                )
                .run(context -> {
                    McpClient client = context.getBean(McpClient.class);
                    assertThat(client.key()).isEqualTo("local");
                    assertThat(stdioTestServerProcesses()).isEmpty();

                    assertThat(client.listTools())
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
                .run(context -> {
                    assertThatThrownBy(() -> context.getBean(McpClient.class).listTools())
                            .hasMessageContaining("failing on purpose");

                    for (ProcessHandle process : stdioTestServerProcesses()) {
                        process.onExit().get(10, SECONDS);
                    }
                    assertThat(stdioTestServerProcesses()).isEmpty();
                });
    }

    @Test
    void should_close_connected_clients_when_context_is_closed() throws IOException {
        AtomicReference<McpClient> client = new AtomicReference<>();
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> {
                        client.set(context.getBean(McpClient.class));
                        client.get().listTools();
                    });
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
                            TOOL_PROVIDER_ENABLED,
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
                        TOOL_PROVIDER_ENABLED,
                        "langchain4j.mcp.clients.files.transport=STDIO",
                        "langchain4j.mcp.clients.files.command[0]=unused"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(McpClient.class);
                    assertThat(context).doesNotHaveBean(ToolProvider.class);
                });
    }

    @Test
    void should_use_configured_protocol_version() throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url(),
                            "langchain4j.mcp.clients.files.protocol-version=2025-11-25"
                    )
                    .run(context -> {
                        context.getBean(McpClient.class).listTools();

                        assertThat(server.methods()).first().isEqualTo("initialize");
                    });
        }
    }

    @Test
    void should_use_configured_initialization_timeout() throws URISyntaxException {
        contextRunner
                .withPropertyValues(stdioTestServerCommand("silent"))
                .withPropertyValues(
                        "langchain4j.mcp.clients.silent.transport=STDIO",
                        "langchain4j.mcp.clients.silent.command[4]=hang",
                        "langchain4j.mcp.clients.silent.initialization-timeout=100ms"
                )
                .run(context -> assertTimeout(Duration.ofSeconds(10), () ->
                        assertThatThrownBy(() -> context.getBean(McpClient.class).listTools())
                                .isInstanceOf(RuntimeException.class)));
    }

    @Test
    void should_use_configured_tool_execution_timeout() throws URISyntaxException {
        contextRunner
                .withPropertyValues(stdioTestServerCommand("local"))
                .withPropertyValues(
                        "langchain4j.mcp.clients.local.transport=STDIO",
                        "langchain4j.mcp.clients.local.tool-execution-timeout=100ms"
                )
                .run(context -> assertTimeout(Duration.ofSeconds(10), () -> {
                    ToolExecutionResult result = context.getBean(McpClient.class)
                            .executeTool(ToolExecutionRequest.builder().name("echo_environment").arguments("{}").build());

                    assertThat(result.isError()).isTrue();
                    assertThat(result.resultText()).isEqualTo("There was a timeout executing the tool");
                }));
    }

    @Test
    void should_notify_mcp_client_listener_beans() throws IOException {
        McpClientListener listener = mock(McpClientListener.class);
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withBean(McpClientListener.class, () -> listener)
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url()
                    )
                    .run(context -> {
                        context.getBean(McpClient.class).listTools();

                        verify(listener).afterServerDiscover(any(), any());
                    });
        }
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void should_log_http_traffic_when_enabled(CapturedOutput output) throws IOException {
        try (McpProtocolTestServer server = new McpProtocolTestServer("read_file")) {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.mcp.clients.files.transport=STREAMABLE_HTTP",
                            "langchain4j.mcp.clients.files.url=" + server.url(),
                            "langchain4j.mcp.clients.files.log-requests=true"
                    )
                    .run(context -> context.getBean(McpClient.class).listTools());
        }

        assertThat(output).contains("Request: ").contains("tools/list");
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
        String expected = "MCP client 'mixed' uses the STDIO transport, but 'url', 'custom-headers', "
                + "'log-requests' and 'log-responses' apply only to the STREAMABLE_HTTP transport";
        String transport = "langchain4j.mcp.clients.mixed.transport=STDIO";
        String command = "langchain4j.mcp.clients.mixed.command[0]=unused";

        assertStartupFailure(expected, transport, command, "langchain4j.mcp.clients.mixed.url=http://localhost:1/mcp");
        assertStartupFailure(expected, transport, command, "langchain4j.mcp.clients.mixed.custom-headers.Authorization=Bearer token");
        assertStartupFailure(expected, transport, command, "langchain4j.mcp.clients.mixed.log-requests=true");
        assertStartupFailure(expected, transport, command, "langchain4j.mcp.clients.mixed.log-responses=true");
    }

    @Test
    void should_fail_when_streamable_http_client_sets_stdio_properties() {
        String expected = "MCP client 'mixed' uses the STREAMABLE_HTTP transport, "
                + "but 'command', 'environment' and 'log-events' apply only to the STDIO transport";
        String transport = "langchain4j.mcp.clients.mixed.transport=STREAMABLE_HTTP";
        String url = "langchain4j.mcp.clients.mixed.url=http://localhost:1/mcp";

        assertStartupFailure(expected, transport, url, "langchain4j.mcp.clients.mixed.command[0]=unused");
        assertStartupFailure(expected, transport, url, "langchain4j.mcp.clients.mixed.environment.API_KEY=secret");
        assertStartupFailure(expected, transport, url, "langchain4j.mcp.clients.mixed.log-events=true");
    }

    private void assertStartupFailure(String expectedMessage, String... properties) {
        contextRunner
                .withPropertyValues(properties)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining(expectedMessage);
                });
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
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
        private final List<String> methods = new CopyOnWriteArrayList<>();
        private final AtomicBoolean failing = new AtomicBoolean();

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
                String methodName = method.find() ? method.group(1) : "";
                methods.add(methodName);
                String body = failing.get()
                        ? "\"error\":{\"code\":-32021,\"message\":\"failing on purpose\"}"
                        : "\"result\":" + (methodName.equals("tools/list")
                                ? "{\"tools\":[{\"name\":\"" + toolName + "\",\"inputSchema\":{\"type\":\"object\",\"properties\":{}}}]}"
                                : "{\"supportedVersions\":[\"2026-07-28\"],\"protocolVersion\":\"2025-11-25\",\"capabilities\":{\"tools\":{}}}");
                byte[] response = ("{\"jsonrpc\":\"2.0\",\"id\":" + id.group(1) + "," + body + "}")
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

        private List<String> methods() {
            return methods;
        }

        private void failing(boolean failing) {
            this.failing.set(failing);
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
