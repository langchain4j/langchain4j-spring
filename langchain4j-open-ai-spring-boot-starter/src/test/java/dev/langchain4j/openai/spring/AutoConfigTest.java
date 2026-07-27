package dev.langchain4j.openai.spring;

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void shouldConfigureProxyForChatModelHttpClientBuilder() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.chat-model.proxy.type=HTTP",
                        "langchain4j.open-ai.chat-model.proxy.host=proxy.example.com",
                        "langchain4j.open-ai.chat-model.proxy.port=8080")
                .run(context -> assertProxy(context.getBean(
                        "openAiChatModelHttpClientBuilder",
                        SpringRestClientBuilder.class)));
    }

    @Test
    void shouldConfigureProxyForImageModelHttpClientBuilder() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.image-model.api-key=test-api-key",
                        "langchain4j.open-ai.image-model.proxy.type=HTTP",
                        "langchain4j.open-ai.image-model.proxy.host=proxy.example.com",
                        "langchain4j.open-ai.image-model.proxy.port=8080")
                .run(context -> assertProxy(context.getBean(
                        "openAiImageModelHttpClientBuilder",
                        SpringRestClientBuilder.class)));
    }

    private static void assertProxy(SpringRestClientBuilder httpClientBuilder) {
        Proxy proxy = httpClientBuilder.proxy();
        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);

        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertThat(address.getHostString()).isEqualTo("proxy.example.com");
        assertThat(address.getPort()).isEqualTo(8080);
    }
}
