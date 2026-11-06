package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;

@AutoConfiguration
@EnableConfigurationProperties(McpProperties.class)
public class McpAutoConfiguration {

    @Bean
    static McpClientBeanRegistrar mcpClientBeanRegistrar(Environment environment) {
        return new McpClientBeanRegistrar(environment);
    }

    @Bean
    @Conditional(McpClientsConfiguredCondition.class)
    @ConditionalOnMissingBean(ToolProvider.class)
    McpToolProvider mcpToolProvider(ObjectProvider<McpClient> clients) {
        return McpToolProvider.builder().mcpClients(clients.orderedStream().toList()).build();
    }
}
