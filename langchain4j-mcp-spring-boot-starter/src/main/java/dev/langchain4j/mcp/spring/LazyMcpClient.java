package dev.langchain4j.mcp.spring;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.internal.AsyncNotSupported;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpGetPromptResult;
import dev.langchain4j.mcp.client.McpPrompt;
import dev.langchain4j.mcp.client.McpReadResourceResult;
import dev.langchain4j.mcp.client.McpResource;
import dev.langchain4j.mcp.client.McpResourceTemplate;
import dev.langchain4j.mcp.client.McpRoot;
import dev.langchain4j.service.tool.ToolExecutionResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Connects to the MCP server on the first call, so that a server that is down does not stop the application from
 * starting. A failed connection is not remembered: the next call tries again.
 */
final class LazyMcpClient implements McpClient {

    private final String key;
    private final Supplier<McpClient> connector;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile McpClient client;
    private boolean closed;

    LazyMcpClient(String key, Supplier<McpClient> connector) {
        this.key = key;
        this.connector = connector;
    }

    private McpClient client() {
        McpClient connected = client;
        if (connected != null) {
            return connected;
        }
        lock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("The client is closed");
            }
            if (client == null) {
                client = connector.get();
            }
            return client;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String instructions() {
        return client().instructions();
    }

    @Override
    public List<ToolSpecification> listTools() {
        return client().listTools();
    }

    @Override
    public List<ToolSpecification> listTools(InvocationContext invocationContext) {
        return client().listTools(invocationContext);
    }

    @Override
    public ToolExecutionResult executeTool(ToolExecutionRequest executionRequest) {
        return client().executeTool(executionRequest);
    }

    @Override
    public ToolExecutionResult executeTool(ToolExecutionRequest executionRequest, InvocationContext invocationContext) {
        return client().executeTool(executionRequest, invocationContext);
    }

    @Override
    public CompletableFuture<ToolExecutionResult> executeToolAsync(
            ToolExecutionRequest executionRequest, InvocationContext invocationContext) {
        McpClient connected = client;
        if (connected == null) {
            return AsyncNotSupported.failedFuture("MCP client '" + key + "' is not connected yet and connecting blocks."
                    + " Call a blocking method, such as listTools(), first");
        }
        return connected.executeToolAsync(executionRequest, invocationContext);
    }

    @Override
    public List<McpResource> listResources() {
        return client().listResources();
    }

    @Override
    public List<McpResource> listResources(InvocationContext invocationContext) {
        return client().listResources(invocationContext);
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates() {
        return client().listResourceTemplates();
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates(InvocationContext invocationContext) {
        return client().listResourceTemplates(invocationContext);
    }

    @Override
    public McpReadResourceResult readResource(String uri) {
        return client().readResource(uri);
    }

    @Override
    public McpReadResourceResult readResource(String uri, InvocationContext invocationContext) {
        return client().readResource(uri, invocationContext);
    }

    @Override
    public void subscribeToResource(String uri) {
        client().subscribeToResource(uri);
    }

    @Override
    public void unsubscribeFromResource(String uri) {
        client().unsubscribeFromResource(uri);
    }

    @Override
    public long subscribeToResources(List<String> uris) {
        return client().subscribeToResources(uris);
    }

    @Override
    public void unsubscribeFromResources(long subscriptionId) {
        client().unsubscribeFromResources(subscriptionId);
    }

    @Override
    public List<McpPrompt> listPrompts() {
        return client().listPrompts();
    }

    @Override
    public McpGetPromptResult getPrompt(String name, Map<String, Object> arguments) {
        return client().getPrompt(name, arguments);
    }

    @Override
    public void checkHealth() {
        client().checkHealth();
    }

    @Override
    public void setRoots(List<McpRoot> roots) {
        client().setRoots(roots);
    }

    @Override
    public void close() throws Exception {
        lock.lock();
        try {
            closed = true;
            if (client != null) {
                client.close();
            }
        } finally {
            lock.unlock();
        }
    }
}
