package dev.langchain4j.mcp.spring;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

final class McpClientsConfiguredCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean matches = !McpClientBeanRegistrar.configuredClients(context.getEnvironment()).isEmpty();
        return new ConditionOutcome(matches, matches
                ? "MCP clients are configured"
                : "MCP clients are not configured or are disabled");
    }
}
