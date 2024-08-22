package dev.langchain4j.service.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AiServiceScannerProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathAiServiceScanner classPathInterfaceScanner = new ClassPathAiServiceScanner(registry, false);
        classPathInterfaceScanner.registerFilters();
        Set<String> basePackages = getBasePackages((ConfigurableListableBeanFactory) registry);
        for (String basePackage : basePackages) {
            classPathInterfaceScanner.scan(basePackage);
        }
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

        String[] applicationBean = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
        SpringBootApplication springbootApplication = AnnotationUtils.findAnnotation(beanFactory.getType(applicationBean[0]), SpringBootApplication.class);
        if (springbootApplication != null) {
            Collections.addAll(basePackages, springbootApplication.scanBasePackages());
            for (Class<?> aClass : springbootApplication.scanBasePackageClasses()) {
                basePackages.add(ClassUtils.getPackageName(aClass));
            }
        }

        return basePackages;
    }
}
