package dev.langchain4j.service.spring;

import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AiServiceScannerProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private volatile Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathAiServiceScanner scanner = new ClassPathAiServiceScanner(registry, false);
        Set<String> basePackages = getBasePackages((ConfigurableListableBeanFactory) registry);
        scanner.scan(StringUtils.toStringArray(basePackages));
    }

    private Set<String> getBasePackages(ConfigurableListableBeanFactory beanFactory) {
        Set<String> basePackages = new LinkedHashSet<>();

        // AutoConfiguration
        List<String> autoConfigPackages = AutoConfigurationPackages.get(beanFactory);
        basePackages.addAll(autoConfigPackages);

        // ComponentScan
        addComponentScanPackages(beanFactory, basePackages);

        return basePackages;
    }

    private void addComponentScanPackages(ConfigurableListableBeanFactory beanFactory, Set<String> collectedBasePackages) {
        beanFactory.getBeansWithAnnotation(ComponentScan.class).forEach((beanName, instance) -> {
            Set<ComponentScan> componentScans = AnnotatedElementUtils.getMergedRepeatableAnnotations(instance.getClass(), ComponentScan.class);
            for (ComponentScan componentScan : componentScans) {
                Set<String> basePackages = new LinkedHashSet<>();
                String[] basePackagesArray = componentScan.basePackages();
                for (String pkg : basePackagesArray) {
                    String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg),
                            ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
                    Collections.addAll(basePackages, tokenized);
                }
                for (Class<?> clazz : componentScan.basePackageClasses()) {
                    basePackages.add(ClassUtils.getPackageName(clazz));
                }
                if (basePackages.isEmpty()) {
                    basePackages.add(ClassUtils.getPackageName(instance.getClass()));
                }
                collectedBasePackages.addAll(basePackages);
            }
        });
    }
}