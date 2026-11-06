package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;

final class McpClientFactory {

    private McpClientFactory() {
    }

    static DefaultMcpClient create(String name, McpProperties.Client properties) {
        McpProperties.Transport transportType = properties.getTransport();
        if (transportType == null) {
            throw new IllegalArgumentException("MCP client '" + name + "' must define a transport");
        }

        return switch (transportType) {
            case HTTP -> {
                String url = properties.getUrl();
                if (url == null || url.isBlank()) {
                    throw new IllegalArgumentException("HTTP MCP client '" + name + "' must define a URL");
                }
                StreamableHttpMcpTransport.Builder transport = StreamableHttpMcpTransport.builder().url(url);
                if (!properties.getHeaders().isEmpty()) {
                    transport.customHeaders(properties.getHeaders());
                }
                yield DefaultMcpClient.builder().transport(transport.build()).build();
            }
            case STDIO -> {
                if (properties.getCommand().isEmpty()) {
                    throw new IllegalArgumentException("STDIO MCP client '" + name + "' must define a command");
                }
                StdioMcpTransport.Builder transport = StdioMcpTransport.builder()
                        .command(properties.getCommand());
                if (!properties.getEnvironment().isEmpty()) {
                    transport.environment(properties.getEnvironment());
                }
                yield DefaultMcpClient.builder().transport(transport.build()).build();
            }
        };
    }
}
