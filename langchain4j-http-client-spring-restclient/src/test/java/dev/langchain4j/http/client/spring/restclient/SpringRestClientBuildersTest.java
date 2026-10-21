package dev.langchain4j.http.client.spring.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorSettings;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class SpringRestClientBuildersTest {

    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @Test
    void should_use_the_beans_of_the_application() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        WebClient.Builder webClientBuilder = WebClient.builder();
        ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder = ClientHttpRequestFactoryBuilder.jdk();
        ClientHttpRequestFactorySettings requestFactorySettings = ClientHttpRequestFactorySettings.defaults();
        ClientHttpConnectorBuilder<?> connectorBuilder = ClientHttpConnectorBuilder.jdk();
        ClientHttpConnectorSettings connectorSettings = ClientHttpConnectorSettings.defaults();
        register("restClientBuilder", RestClient.Builder.class, () -> restClientBuilder, false);
        register("webClientBuilder", WebClient.Builder.class, () -> webClientBuilder, false);
        register("requestFactoryBuilder", ClientHttpRequestFactoryBuilder.class, () -> requestFactoryBuilder, false);
        register("requestFactorySettings", ClientHttpRequestFactorySettings.class, () -> requestFactorySettings, false);
        register("connectorBuilder", ClientHttpConnectorBuilder.class, () -> connectorBuilder, false);
        register("connectorSettings", ClientHttpConnectorSettings.class, () -> connectorSettings, false);

        SpringRestClientBuilder builder = SpringRestClientBuilders.fromSpringBoot(beanFactory);

        assertThat(builder.restClientBuilder()).isSameAs(restClientBuilder);
        assertThat(builder.webClientBuilder().webClientBuilder()).isSameAs(webClientBuilder);
        assertThat(builder.clientHttpRequestFactoryBuilder()).isSameAs(requestFactoryBuilder);
        assertThat(builder.clientHttpRequestFactorySettings()).isSameAs(requestFactorySettings);
        assertThat(builder.clientHttpConnectorBuilder()).isSameAs(connectorBuilder);
        assertThat(builder.clientHttpConnectorSettings()).isSameAs(connectorSettings);
    }

    @Test
    void should_use_new_builders_and_the_defaults_without_beans() {

        SpringRestClientBuilder builder = SpringRestClientBuilders.fromSpringBoot(beanFactory);

        assertThat(builder.restClientBuilder()).isNotNull();
        assertThat(builder.webClientBuilder().webClientBuilder()).isNotNull();
        assertThat(builder.clientHttpRequestFactoryBuilder()).isNull();
        assertThat(builder.clientHttpRequestFactorySettings()).isNull();
        assertThat(builder.clientHttpConnectorBuilder()).isNull();
        assertThat(builder.clientHttpConnectorSettings()).isNull();
    }

    @Test
    void should_use_the_primary_bean_when_there_are_several() {
        ClientHttpConnectorSettings primary = ClientHttpConnectorSettings.defaults().withRedirects(HttpRedirects.DONT_FOLLOW);
        register("first", ClientHttpConnectorSettings.class, ClientHttpConnectorSettings::defaults, false);
        register("second", ClientHttpConnectorSettings.class, () -> primary, true);

        SpringRestClientBuilder builder = SpringRestClientBuilders.fromSpringBoot(beanFactory);

        assertThat(builder.clientHttpConnectorSettings()).isSameAs(primary);
    }

    @Test
    void should_use_none_of_several_beans_when_none_of_them_is_primary() {
        register("first", ClientHttpRequestFactorySettings.class, ClientHttpRequestFactorySettings::defaults, false);
        register("second", ClientHttpRequestFactorySettings.class, ClientHttpRequestFactorySettings::defaults, false);
        register("firstRestClientBuilder", RestClient.Builder.class, RestClient::builder, false);
        register("secondRestClientBuilder", RestClient.Builder.class, RestClient::builder, false);
        register("firstWebClientBuilder", WebClient.Builder.class, WebClient::builder, false);
        register("secondWebClientBuilder", WebClient.Builder.class, WebClient::builder, false);

        SpringRestClientBuilder builder = SpringRestClientBuilders.fromSpringBoot(beanFactory);

        assertThat(builder.clientHttpRequestFactorySettings()).isNull();
        assertThat(builder.restClientBuilder())
                .isNotNull()
                .isNotSameAs(beanFactory.getBean("firstRestClientBuilder"))
                .isNotSameAs(beanFactory.getBean("secondRestClientBuilder"));
        assertThat(builder.webClientBuilder().webClientBuilder())
                .isNotSameAs(beanFactory.getBean("firstWebClientBuilder"))
                .isNotSameAs(beanFactory.getBean("secondWebClientBuilder"));
    }

    @Test
    void should_prefer_a_web_client_builder_holder_of_the_application() {
        WebClientBuilderHolder holder = WebClientBuilderHolder.of(WebClient.builder());
        register("holder", WebClientBuilderHolder.class, () -> holder, false);
        register("webClientBuilder", WebClient.Builder.class, WebClient::builder, false);

        SpringRestClientBuilder builder = SpringRestClientBuilders.fromSpringBoot(beanFactory);

        assertThat(builder.webClientBuilder()).isSameAs(holder);
    }

    private <T> void register(String name, Class<T> type, Supplier<? extends T> supplier, boolean primary) {
        RootBeanDefinition definition = new RootBeanDefinition(type, supplier::get);
        definition.setPrimary(primary);
        beanFactory.registerBeanDefinition(name, definition);
    }
}
