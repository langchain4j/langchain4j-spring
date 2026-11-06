package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;

import java.io.IOException;

import static dev.langchain4j.mcp.spring.McpProperties.PREFIX;

final class McpClientFactory {

    private McpClientFactory() {
    }

    static DefaultMcpClient create(String name, McpProperties.Client properties) {
        McpTransport transport = createTransport(name, properties);
        try {
            return DefaultMcpClient.builder()
                    .key(name)
                    .transport(transport)
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

    private static McpTransport createTransport(String name, McpProperties.Client properties) {
        String clientPrefix = PREFIX + ".clients." + name;
        McpProperties.Transport transportType = properties.getTransport();
        if (transportType == null) {
            throw new IllegalArgumentException("MCP client '" + name + "' has no transport. Set '"
                    + clientPrefix + ".transport' to STREAMABLE_HTTP or STDIO");
        }

        return switch (transportType) {
            case STREAMABLE_HTTP -> {
                if (!properties.getCommand().isEmpty() || !properties.getEnvironment().isEmpty()) {
                    throw new IllegalArgumentException("MCP client '" + name + "' uses the STREAMABLE_HTTP transport, "
                            + "but 'command' and 'environment' apply only to the STDIO transport");
                }
                String url = properties.getUrl();
                if (url == null || url.isBlank()) {
                    throw new IllegalArgumentException("MCP client '" + name + "' has no URL. Set '"
                            + clientPrefix + ".url' to the URL of the MCP server endpoint");
                }
                yield StreamableHttpMcpTransport.builder()
                        .url(url)
                        .customHeaders(properties.getCustomHeaders())
                        .build();
            }
            case STDIO -> {
                if (properties.getUrl() != null || !properties.getCustomHeaders().isEmpty()) {
                    throw new IllegalArgumentException("MCP client '" + name + "' uses the STDIO transport, "
                            + "but 'url' and 'custom-headers' apply only to the STREAMABLE_HTTP transport");
                }
                if (properties.getCommand().isEmpty()) {
                    throw new IllegalArgumentException("MCP client '" + name + "' has no command. Set '"
                            + clientPrefix + ".command' to the program that starts the MCP server and its arguments");
                }
                yield StdioMcpTransport.builder()
                        .command(properties.getCommand())
                        .environment(properties.getEnvironment())
                        .build();
            }
        };
    }
}
