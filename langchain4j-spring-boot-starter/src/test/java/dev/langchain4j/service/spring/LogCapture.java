package dev.langchain4j.service.spring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.joining;

/**
 * Captures a logger's events at DEBUG level, independently of any console appender,
 * and restores the logger's previous level on {@link #close()}.
 */
public final class LogCapture implements AutoCloseable {

    private final Logger logger;
    private final Level previousLevel;
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private LogCapture(Class<?> loggerClass) {
        this.logger = (Logger) LoggerFactory.getLogger(loggerClass);
        this.previousLevel = logger.getLevel();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
    }

    public static LogCapture debug(Class<?> loggerClass) {
        return new LogCapture(loggerClass);
    }

    public String messages() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(joining(System.lineSeparator()));
    }

    @Override
    public void close() {
        logger.setLevel(previousLevel);
        logger.detachAppender(appender);
        appender.stop();
    }
}
