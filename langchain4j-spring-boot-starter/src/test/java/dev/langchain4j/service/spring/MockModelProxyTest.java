package dev.langchain4j.service.spring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.test.mock.MockChatModel;
import dev.langchain4j.test.mock.MockDecisionModel;
import dev.langchain4j.test.mock.MockEmbeddingModel;
import dev.langchain4j.test.mock.MockModerationModel;
import dev.langchain4j.test.mock.MockScoringModel;
import dev.langchain4j.test.mock.MockStreamingChatModel;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    static Stream<Object> mocks() {
        return Stream.of(
                MockChatModel.thatResponds("Hi"),
                MockStreamingChatModel.thatResponds("Hi"),
                MockEmbeddingModel.thatEmbeds(1f, 2f, 3f),
                MockScoringModel.thatScores(0.5),
                MockDecisionModel.thatAnswersYesNoQuestions(question -> 0.9),
                MockModerationModel.thatNeverFlags());
    }

    @ParameterizedTest
    @MethodSource("mocks")
    void should_proxy_every_mock_by_its_class(Object target) {

        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);

        Object proxy = proxyFactory.getProxy();

        assertThat(proxy).isInstanceOf(target.getClass());
        assertThat(proxy.getClass()).isNotEqualTo(target.getClass());
    }
}
