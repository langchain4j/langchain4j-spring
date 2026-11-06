package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Map;

import static dev.langchain4j.mcp.spring.McpProperties.PREFIX;

final class McpClientBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

    private final Environment environment;

    McpClientBeanRegistrar(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        configuredClients(environment).forEach((name, clientProperties) -> {
            String beanName = "mcpClient." + name;
            if (!registry.containsBeanDefinition(beanName)) {
                registry.registerBeanDefinition(beanName, new RootBeanDefinition(
                        DefaultMcpClient.class, () -> McpClientFactory.create(name, clientProperties)));
            }
        });
    }

    static Map<String, McpProperties.Client> configuredClients(Environment environment) {
        McpProperties properties = Binder.get(environment)
                .bind(PREFIX, McpProperties.class)
                .orElseGet(McpProperties::new);
        return properties.isEnabled() ? properties.getClients() : Map.of();
    }
}
