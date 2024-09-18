package dev.langchain4j.service.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.*;

@Component
public class AiServiceScannerProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathAiServiceScanner classPathAiServiceScanner = new ClassPathAiServiceScanner(registry, false);
        Set<String> basePackages = getBasePackages((ConfigurableListableBeanFactory) registry);
        for (String basePackage : basePackages) {
            classPathAiServiceScanner.scan(basePackage);
        }

        removeAiServicesWithInactiveProfiles(registry);
    }

    private Set<String> getBasePackages(ConfigurableListableBeanFactory beanFactory) {
        Set<String> basePackages = new LinkedHashSet<>();

        List<String> autoConfigPackages = AutoConfigurationPackages.get(beanFactory);
        basePackages.addAll(autoConfigPackages);

        String[] beanNames = beanFactory.getBeanNamesForAnnotation(ComponentScan.class);
        for (String beanName : beanNames) {
            Class<?> beanClass = beanFactory.getType(beanName);
            if (beanClass != null) {
                ComponentScan componentScan = beanClass.getAnnotation(ComponentScan.class);
                if (componentScan != null) {
                    Collections.addAll(basePackages, componentScan.value());
                    Collections.addAll(basePackages, componentScan.basePackages());
                    for (Class<?> basePackageClass : componentScan.basePackageClasses()) {
                        basePackages.add(basePackageClass.getPackage().getName());
                    }
                }
            }
        }

        String[] applicationBeans = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
        if (applicationBeans.length > 0) {
            Class<?> applicationBeanClass = beanFactory.getType(applicationBeans[0]);
            SpringBootApplication springBootApplication = AnnotationUtils.findAnnotation(applicationBeanClass, SpringBootApplication.class);
            if (springBootApplication != null) {
                Collections.addAll(basePackages, springBootApplication.scanBasePackages());
                for (Class<?> aClass : springBootApplication.scanBasePackageClasses()) {
                    basePackages.add(ClassUtils.getPackageName(aClass));
                }
            }
        }

        return basePackages;
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
