package dev.langchain4j.mcp.spring;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.springframework.boot.context.properties.bind.Bindable.mapOf;
import static org.springframework.boot.context.properties.bind.Binder.get;

final class McpClientBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

    private final Environment environment;

    McpClientBeanRegistrar(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        McpProperties properties = get(environment)
                .bind("langchain4j.mcp", McpProperties.class)
                .orElseGet(McpProperties::new);
        if (!properties.isEnabled()) {
            return;
        }

        Map<String, McpProperties.Client> clients = get(environment)
                .bind("langchain4j.mcp.clients", mapOf(String.class, McpProperties.Client.class))
                .orElseGet(Map::of);

        clients.forEach((name, clientProperties) -> {
            String beanName = "mcpClient." + name;
            if (!registry.containsBeanDefinition(beanName)) {
                RootBeanDefinition beanDefinition = new RootBeanDefinition(
                        dev.langchain4j.mcp.client.DefaultMcpClient.class);
                beanDefinition.setInstanceSupplier(() -> McpClientFactory.create(name, clientProperties));
                beanDefinition.setDestroyMethodName("close");
                registry.registerBeanDefinition(beanName, beanDefinition);
            }
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }
}
