package dev.langchain4j.http.client.spring.restclient;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;

/**
 * The Spring Boot 3.5+ implementation uses reflection because Spring Boot 4 is no longer included in the langchain4J-spring classpath.
 */
class SpringBoot3HttpClientSettings implements HttpClientSettingsStrategy {

    private static final Class<?> DEPRECATED_SETTINGS_CLASS;
    private static final Class<?> NEW_SETTINGS_CLASS;
    private static final Field DEFAULTS_FIELD;
    private static final Method WITH_CONNECT_TIMEOUT_METHOD;
    private static final Method WITH_READ_TIMEOUT_METHOD;
    private static final Method ADAPT_METHOD;
    private static final Method BUILD_METHOD;

    static {
        try {
            DEPRECATED_SETTINGS_CLASS = Class.forName("org.springframework.boot.web.client.ClientHttpRequestFactorySettings");
            NEW_SETTINGS_CLASS = Class.forName("org.springframework.boot.http.client.ClientHttpRequestFactorySettings");
            
            DEFAULTS_FIELD = DEPRECATED_SETTINGS_CLASS.getField("DEFAULTS");
            WITH_CONNECT_TIMEOUT_METHOD = DEPRECATED_SETTINGS_CLASS.getMethod("withConnectTimeout", Duration.class);
            WITH_READ_TIMEOUT_METHOD = DEPRECATED_SETTINGS_CLASS.getMethod("withReadTimeout", Duration.class);
            ADAPT_METHOD = DEPRECATED_SETTINGS_CLASS.getDeclaredMethod("adapt");
            ADAPT_METHOD.setAccessible(true);
            
            ClientHttpRequestFactoryBuilder<?> builder = ClientHttpRequestFactoryBuilder.detect();
            BUILD_METHOD = findBuildMethod(builder.getClass(), NEW_SETTINGS_CLASS);
            BUILD_METHOD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to initialize SpringBoot3HttpClientSettings", e);
        }
    }

    @Override
    public ClientHttpRequestFactory createRequestFactory(Duration connectTimeout, Duration readTimeout) {
        try {
            Object deprecatedSettings = DEFAULTS_FIELD.get(null);
            
            if (connectTimeout != null) {
                deprecatedSettings = WITH_CONNECT_TIMEOUT_METHOD.invoke(deprecatedSettings, connectTimeout);
            }
            if (readTimeout != null) {
                deprecatedSettings = WITH_READ_TIMEOUT_METHOD.invoke(deprecatedSettings, readTimeout);
            }

            Object newSettings = ADAPT_METHOD.invoke(deprecatedSettings);
            return (ClientHttpRequestFactory) BUILD_METHOD.invoke(ClientHttpRequestFactoryBuilder.detect(), newSettings);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create ClientHttpRequestFactory for Spring Boot 3.x", e);
        }
    }

    private static Method findBuildMethod(Class<?> clazz, Class<?> newSettingsClass) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if ("build".equals(method.getName())
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(newSettingsClass)) {
                return method;
            }
        }
        if (clazz.getSuperclass() != null) {
            return findBuildMethod(clazz.getSuperclass(), newSettingsClass);
        }
        throw new NoSuchMethodException("No build method accepting " + newSettingsClass.getName());
    }
}
