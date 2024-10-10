package dev.langchain4j.service.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class AiServiceScannerProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathAiServiceScanner scanner = new ClassPathAiServiceScanner(registry, false);
        Set<String> basePackages = getBasePackages((ConfigurableListableBeanFactory) registry);
        scanner.scan(StringUtils.toStringArray(basePackages));

        removeAiServicesWithInactiveProfiles(registry);
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

    private void removeAiServicesWithInactiveProfiles(BeanDefinitionRegistry registry) {
        Arrays.stream(registry.getBeanDefinitionNames())
                .filter(beanName -> {
                    try {
                        BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
                        if (beanDefinition.getBeanClassName() != null) {
                            Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                            if (beanClass.isAnnotationPresent(AiService.class)
                                    && beanClass.isAnnotationPresent(Profile.class)) {
                                Profile profileAnnotation = beanClass.getAnnotation(Profile.class);
                                String[] profiles = profileAnnotation.value();
                                return !environment.matchesProfiles(profiles);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // should not happen
                    }

                    return false;
                }).forEach(registry::removeBeanDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
