package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpClientListener;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.mcp.spring.McpProperties.PREFIX;

final class McpClientFactory {

    private McpClientFactory() {
    }

    static McpClient create(String name, McpProperties.Client properties, Supplier<List<McpClientListener>> listeners) {
        validate(name, properties);
        return new LazyMcpClient(name, () -> connect(name, properties, listeners.get()));
    }

    private static void validate(String name, McpProperties.Client properties) {
        String clientPrefix = PREFIX + ".clients." + name;
        McpProperties.Transport transport = properties.getTransport();
        if (transport == null) {
            throw new IllegalArgumentException("MCP client '" + name + "' has no transport. Set '"
                    + clientPrefix + ".transport' to STREAMABLE_HTTP or STDIO");
        }

        switch (transport) {
            case STREAMABLE_HTTP -> {
                if (!properties.getCommand().isEmpty() || !properties.getEnvironment().isEmpty()
                        || properties.getLogEvents() != null) {
                    throw new IllegalArgumentException("MCP client '" + name + "' uses the STREAMABLE_HTTP transport, "
                            + "but 'command', 'environment' and 'log-events' apply only to the STDIO transport");
                }
                String url = properties.getUrl();
                if (url == null || url.isBlank()) {
                    throw new IllegalArgumentException("MCP client '" + name + "' has no URL. Set '"
                            + clientPrefix + ".url' to the URL of the MCP server endpoint");
                }
            }
            case STDIO -> {
                if (properties.getUrl() != null || !properties.getCustomHeaders().isEmpty()
                        || properties.getLogRequests() != null || properties.getLogResponses() != null) {
                    throw new IllegalArgumentException("MCP client '" + name + "' uses the STDIO transport, but 'url', "
                            + "'custom-headers', 'log-requests' and 'log-responses' apply only to the "
                            + "STREAMABLE_HTTP transport");
                }
                if (properties.getCommand().isEmpty()) {
                    throw new IllegalArgumentException("MCP client '" + name + "' has no command. Set '"
                            + clientPrefix + ".command' to the program that starts the MCP server and its arguments");
                }
            }
        }
    }

    private static McpClient connect(String name, McpProperties.Client properties, List<McpClientListener> listeners) {
        McpTransport transport = createTransport(properties);
        try {
            return DefaultMcpClient.builder()
                    .key(name)
                    .transport(transport)
                    .protocolVersion(properties.getProtocolVersion())
                    .initializationTimeout(properties.getInitializationTimeout())
                    .toolExecutionTimeout(properties.getToolExecutionTimeout())
                    .addListeners(listeners)
                    .build();
        } catch (RuntimeException e) {
            try {
                transport.close();
            } catch (IOException | RuntimeException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
    }

    private static McpTransport createTransport(McpProperties.Client properties) {
        return switch (properties.getTransport()) {
            case STREAMABLE_HTTP -> StreamableHttpMcpTransport.builder()
                    .url(properties.getUrl())
                    .customHeaders(properties.getCustomHeaders())
                    .logRequests(getOrDefault(properties.getLogRequests(), false))
                    .logResponses(getOrDefault(properties.getLogResponses(), false))
                    .build();
            case STDIO -> StdioMcpTransport.builder()
                    .command(properties.getCommand())
                    .environment(properties.getEnvironment())
                    .logEvents(getOrDefault(properties.getLogEvents(), false))
                    .build();
        };
    }
}
