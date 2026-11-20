package dev.langchain4j.mcp.spring;

import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilders;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpClientListener;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.mcp.spring.McpProperties.PREFIX;

final class McpClientFactory {

    private static final boolean WEBFLUX_PRESENT = ClassUtils.isPresent(
            "org.springframework.web.reactive.function.client.WebClient", McpClientFactory.class.getClassLoader());

    private McpClientFactory() {
    }

    static McpClient create(String name,
                            McpProperties.Client properties,
                            Supplier<List<McpClientListener>> listeners,
                            ListableBeanFactory beanFactory) {
        validate(name, properties);
        return new LazyMcpClient(name, () -> connect(name, properties, listeners.get(), beanFactory));
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

    private static McpClient connect(String name,
                                     McpProperties.Client properties,
                                     List<McpClientListener> listeners,
                                     ListableBeanFactory beanFactory) {
        McpTransport transport = createTransport(properties, beanFactory);
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

    private static McpTransport createTransport(McpProperties.Client properties, ListableBeanFactory beanFactory) {
        return switch (properties.getTransport()) {
            case STREAMABLE_HTTP -> withHttpClient(StreamableHttpMcpTransport.builder(), beanFactory)
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

    private static StreamableHttpMcpTransport.Builder withHttpClient(StreamableHttpMcpTransport.Builder transport,
                                                                     ListableBeanFactory beanFactory) {
        if (!WEBFLUX_PRESENT) {
            // the Spring client can stream responses, which MCP needs, only with spring-webflux
            return transport
                    .httpClientBuilder(JdkHttpClient.builder().streamingBackpressure(true))
                    .timeout(Duration.ofSeconds(60));
        }
        // no read timeout: MCP responses and notification streams can stay idle for longer than a timeout set for the whole application
        SpringRestClientBuilder httpClient = SpringRestClientBuilders.fromSpringBoot(beanFactory).readTimeout(Duration.ZERO);
        // no redirects, as with the JDK client: a redirect could send the custom headers and the request to another host
        HttpClientSettings settings = getOrDefault(httpClient.httpClientSettings(), HttpClientSettings::defaults);
        httpClient.httpClientSettings(settings.withRedirects(HttpRedirects.DONT_FOLLOW));
        return transport.httpClientBuilder(httpClient);
    }
}
