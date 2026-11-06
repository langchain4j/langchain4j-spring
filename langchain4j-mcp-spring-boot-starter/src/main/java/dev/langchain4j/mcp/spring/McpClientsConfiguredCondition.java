package dev.langchain4j.mcp.spring;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

final class McpClientsConfiguredCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        McpProperties properties = Binder.get(context.getEnvironment())
                .bind("langchain4j.mcp", McpProperties.class)
                .orElseGet(McpProperties::new);
        Map<String, McpProperties.Client> clients = Binder.get(context.getEnvironment())
                .bind("langchain4j.mcp.clients", Bindable.mapOf(String.class, McpProperties.Client.class))
                .orElseGet(Map::of);
        boolean matches = properties.isEnabled() && !clients.isEmpty();
        return new ConditionOutcome(matches, matches
                ? "MCP clients are configured"
                : "MCP clients are not configured or are disabled");
    }
}
