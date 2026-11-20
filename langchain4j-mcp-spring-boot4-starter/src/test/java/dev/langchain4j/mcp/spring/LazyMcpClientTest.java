package dev.langchain4j.mcp.spring;

import dev.langchain4j.exception.AsyncNotSupportedException;
import dev.langchain4j.mcp.client.McpClient;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LazyMcpClientTest {

    @Test
    void should_delegate_every_method_of_mcp_client() throws Exception {
        // reflection, so that a method added to McpClient later cannot be forgotten here
        McpClient delegate = mock(McpClient.class);
        LazyMcpClient client = new LazyMcpClient("files", () -> delegate);
        client.checkHealth();
        clearInvocations(delegate);

        for (Method method : McpClient.class.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.getName().equals("key")) {
                continue;
            }
            LazyMcpClient.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            Object[] arguments = Arrays.stream(method.getParameterTypes())
                    .map(type -> type == long.class ? (Object) 1L : null)
                    .toArray();

            method.invoke(client, arguments);

            method.invoke(verify(delegate), arguments);
        }
    }

    @Test
    void should_return_key_without_connecting() {
        AtomicInteger connections = new AtomicInteger();
        LazyMcpClient client = new LazyMcpClient("files", () -> {
            connections.incrementAndGet();
            return mock(McpClient.class);
        });

        assertThat(client.key()).isEqualTo("files");

        assertThat(connections).hasValue(0);
    }

    @Test
    void should_connect_only_once() {
        AtomicInteger connections = new AtomicInteger();
        LazyMcpClient client = new LazyMcpClient("files", () -> {
            connections.incrementAndGet();
            return mock(McpClient.class);
        });

        client.listTools();
        client.listPrompts();

        assertThat(connections).hasValue(1);
    }

    @Test
    void should_connect_again_after_a_failed_connection() {
        AtomicInteger connections = new AtomicInteger();
        LazyMcpClient client = new LazyMcpClient("files", () -> {
            if (connections.incrementAndGet() == 1) {
                throw new IllegalStateException("server is down");
            }
            return mock(McpClient.class);
        });

        assertThatThrownBy(client::listTools).hasMessage("server is down");
        client.listTools();

        assertThat(connections).hasValue(2);
    }

    @Test
    void should_fail_the_future_instead_of_connecting_when_called_asynchronously_before_connecting() {
        AtomicInteger connections = new AtomicInteger();
        LazyMcpClient client = new LazyMcpClient("files", () -> {
            connections.incrementAndGet();
            return mock(McpClient.class);
        });

        assertThat(client.executeToolAsync(null, null))
                .failsWithin(Duration.ofSeconds(1))
                .withThrowableThat()
                .havingCause()
                .isInstanceOf(AsyncNotSupportedException.class)
                .withMessageContaining("MCP client 'files' is not connected yet");

        assertThat(connections).hasValue(0);
    }

    @Test
    void should_not_connect_after_being_closed() throws Exception {
        AtomicInteger connections = new AtomicInteger();
        LazyMcpClient client = new LazyMcpClient("files", () -> {
            connections.incrementAndGet();
            return mock(McpClient.class);
        });

        client.close();

        assertThatThrownBy(client::listTools)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The client is closed");
        assertThat(connections).hasValue(0);
    }

    @Test
    void should_close_the_connected_client() throws Exception {
        McpClient delegate = mock(McpClient.class);
        LazyMcpClient client = new LazyMcpClient("files", () -> delegate);
        client.listTools();

        client.close();

        verify(delegate).close();
    }
}
