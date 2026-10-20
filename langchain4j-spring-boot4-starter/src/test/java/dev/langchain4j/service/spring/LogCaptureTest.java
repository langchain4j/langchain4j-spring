package dev.langchain4j.service.spring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogCaptureTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LogCaptureTest.class);

    @AfterEach
    void resetLogger() {
        logger.setLevel(null);
    }

    @Test
    void should_restore_explicitly_configured_level_and_detach_appender() {
        logger.setLevel(Level.WARN);

        LogCapture logs = LogCapture.debug(LogCaptureTest.class);
        assertThat(logger.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(appenders()).hasSize(1);
        logger.debug("captured");
        logs.close();

        assertThat(logger.getLevel()).isEqualTo(Level.WARN);
        assertThat(appenders()).isEmpty();
        logger.warn("after close");
        assertThat(logs.messages()).isEqualTo("captured");
    }

    @Test
    void should_restore_inherited_level() {
        Level effectiveLevel = logger.getEffectiveLevel();

        try (LogCapture logs = LogCapture.atLevel(LogCaptureTest.class, Level.ERROR)) {
            logger.warn("not captured");
            logger.error("captured");
            assertThat(logs.messages()).isEqualTo("captured");
        }

        assertThat(logger.getLevel()).isNull();
        assertThat(logger.getEffectiveLevel()).isEqualTo(effectiveLevel);
        assertThat(appenders()).isEmpty();
    }

    private List<Appender<ILoggingEvent>> appenders() {
        List<Appender<ILoggingEvent>> appenders = new ArrayList<>();
        logger.iteratorForAppenders().forEachRemaining(appenders::add);
        return appenders;
    }
}
