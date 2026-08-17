package dev.langchain4j.openai.spring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The streaming models reuse the properties of their sync counterparts, but cannot honour all of them. Setting
 * such a property has to be reported, otherwise it looks like it took effect.
 */
class OpenAiIgnoredPropertyWarningTest {

    private ListAppender<ILoggingEvent> appender;
    private ch.qos.logback.classic.Logger logger;

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenAiAutoConfiguration.class));

    @BeforeEach
    void setUp() {
        logger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(OpenAiAutoConfiguration.class);
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
    void should_warn_when_streaming_chat_model_is_given_properties_it_ignores() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-retries=3",
                        "langchain4j.open-ai.streaming-chat-model.supported-capabilities=RESPONSE_FORMAT_JSON_SCHEMA"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(StreamingChatModel.class);

                    assertThat(warnings())
                            .anySatisfy(warning -> assertThat(warning)
                                    .contains("langchain4j.open-ai.streaming-chat-model.max-retries")
                                    .contains("ignored"))
                            .anySatisfy(warning -> assertThat(warning)
                                    .contains("langchain4j.open-ai.streaming-chat-model.supported-capabilities")
                                    .contains("ignored"));
                });
    }

    @Test
    void should_warn_when_streaming_language_model_is_given_properties_it_ignores() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-language-model.api-key=test-api-key",
                        "langchain4j.open-ai.streaming-language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.streaming-language-model.max-retries=3"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(StreamingLanguageModel.class);

                    assertThat(warnings()).anySatisfy(warning -> assertThat(warning)
                            .contains("langchain4j.open-ai.streaming-language-model.max-retries")
                            .contains("ignored"));
                });
    }

    @Test
    void should_not_warn_when_the_ignored_properties_are_not_set() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.temperature=0.0"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(StreamingChatModel.class);
                    assertThat(warnings()).isEmpty();
                });
    }

    @Test
    void should_not_warn_when_the_same_properties_are_set_on_the_sync_models() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-retries=3",
                        "langchain4j.open-ai.chat-model.supported-capabilities=RESPONSE_FORMAT_JSON_SCHEMA",
                        "langchain4j.open-ai.language-model.api-key=test-api-key",
                        "langchain4j.open-ai.language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.language-model.max-retries=3"
                )
                .run(context -> assertThat(warnings()).isEmpty());
    }
}
