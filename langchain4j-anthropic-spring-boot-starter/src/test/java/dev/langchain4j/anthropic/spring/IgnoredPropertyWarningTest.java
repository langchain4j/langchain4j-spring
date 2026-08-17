package dev.langchain4j.anthropic.spring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The streaming chat model reuses the properties of its sync counterpart, but cannot honour all of them. Setting
 * such a property has to be reported, otherwise it looks like it took effect.
 */
class IgnoredPropertyWarningTest {

    private ListAppender<ILoggingEvent> appender;
    private ch.qos.logback.classic.Logger logger;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @BeforeEach
    void setUp() {
        logger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(AutoConfig.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    private List<String> warnings() {
        return appender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    @Test
    void should_warn_when_streaming_chat_model_is_given_a_property_it_ignores() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.anthropic.streaming-chat-model.model-name=claude-haiku-4-5-20251001",
                        "langchain4j.anthropic.streaming-chat-model.max-retries=3"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(StreamingChatModel.class);

                    assertThat(warnings()).anySatisfy(warning -> assertThat(warning)
                            .contains("langchain4j.anthropic.streaming-chat-model.max-retries")
                            .contains("ignored"));
                });
    }

    @Test
    void should_not_warn_when_the_ignored_property_is_not_set() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.anthropic.streaming-chat-model.model-name=claude-haiku-4-5-20251001",
                        "langchain4j.anthropic.streaming-chat-model.temperature=0.0"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(StreamingChatModel.class);
                    assertThat(warnings()).isEmpty();
                });
    }

    @Test
    void should_not_warn_when_the_same_property_is_set_on_the_sync_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.anthropic.chat-model.api-key=test-api-key",
                        "langchain4j.anthropic.chat-model.model-name=claude-haiku-4-5-20251001",
                        "langchain4j.anthropic.chat-model.max-retries=3"
                )
                .run(context -> assertThat(warnings()).isEmpty());
    }
}
