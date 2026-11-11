package dev.langchain4j.service.spring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.test.mock.MockChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Spring Boot proxies beans by their class, for example for an aspect on {@code ChatModel} methods, so a mock that
 * replaces a model bean must be proxyable that way.
 */
class MockModelProxyTest {

    @Test
    void should_proxy_a_mock_chat_model_by_its_class() {

        MockChatModel target = MockChatModel.thatResponds("Hi").thenRepeats();
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);

        MockChatModel proxy = (MockChatModel) proxyFactory.getProxy();

        assertThat(proxy.chat("Hello")).isEqualTo("Hi");
        assertThat(target.requests()).hasSize(1);
    }
}
