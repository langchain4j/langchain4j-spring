# langchain4j-spring
This repository contains Spring Boot starters for popular integrations.
Starters for other integrations will be added with time.

If you have any issues or feature requests, please submit them [here](https://github.com/langchain4j/langchain4j/issues/new/choose).

### [Documentation](https://docs.langchain4j.dev/tutorials/spring-boot-integration)

### MCP clients

The `langchain4j-mcp-spring-boot-starter` and
`langchain4j-mcp-spring-boot4-starter` artifacts configure named MCP clients from
`langchain4j.mcp.clients`. Each client uses either the `STREAMABLE_HTTP` or the
`STDIO` transport and is registered as `mcpClient.<name>`. When at least one
client is configured, an `McpToolProvider` is also registered unless the
application supplies its own `ToolProvider`. Clients can be injected by name,
for example with `@Qualifier("mcpClient.filesystem") McpClient client`.

For example:

```yaml
langchain4j:
  mcp:
    clients:
      filesystem:
        transport: STDIO
        command: [npx, "-y", "@modelcontextprotocol/server-filesystem", "/tmp"]
      internal-tools:
        transport: STREAMABLE_HTTP
        url: https://mcp.example.com/mcp
        custom-headers:
          X-Environment: internal
```

Set `langchain4j.mcp.enabled=false` to disable configured MCP clients.
