package dev.langchain4j.mcp.spring;

import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpClientListener;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static dev.langchain4j.mcp.spring.McpProperties.PREFIX;

final class McpClientBeanRegistrar implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {

    private final Environment environment;
    private BeanFactory beanFactory;

    McpClientBeanRegistrar(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        Supplier<List<McpClientListener>> listeners =
                () -> beanFactory.getBeanProvider(McpClientListener.class).orderedStream().toList();
        configuredClients(environment).forEach((name, clientProperties) -> {
            String beanName = "mcpClient." + name;
            if (!registry.containsBeanDefinition(beanName)) {
                registry.registerBeanDefinition(beanName, new RootBeanDefinition(
                        McpClient.class, () -> McpClientFactory.create(name, clientProperties, listeners)));
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
