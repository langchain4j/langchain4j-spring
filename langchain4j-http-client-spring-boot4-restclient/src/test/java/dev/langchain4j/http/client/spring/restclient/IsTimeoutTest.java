package dev.langchain4j.http.client.spring.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which exception a read timeout produces depends on the HTTP client Spring picks, and clients that are optional
 * dependencies cannot be referenced by type at all.
 */
class IsTimeoutTest {

    /**
     * Stands in for {@code io.netty.handler.timeout.ReadTimeoutException} after it has been relocated by a shaded
     * distribution: the package is different, the name is not.
     */
    static class ReadTimeoutException extends RuntimeException {
    }

    @Test
    void should_recognise_the_timeout_of_every_client() {
        assertThat(SpringRestClient.isTimeout(new SocketTimeoutException("Read timed out"))).isTrue();
        assertThat(SpringRestClient.isTimeout(new HttpTimeoutException("Request cancelled"))).isTrue();
        assertThat(SpringRestClient.isTimeout(new ReadTimeoutException())).isTrue();
    }

    @Test
    void should_look_at_the_whole_cause_chain() {
        // the Reactor client nests its timeout, so only the root of the chain says what happened
        assertThat(SpringRestClient.isTimeout(
                        new ResourceAccessException("I/O error", new IOException(new ReadTimeoutException()))))
                .isTrue();
    }

    @Test
    void should_not_treat_other_failures_as_timeouts() {
        assertThat(SpringRestClient.isTimeout(new ResourceAccessException("connection refused"))).isFalse();
        assertThat(SpringRestClient.isTimeout(new IllegalStateException("boom"))).isFalse();
        assertThat(SpringRestClient.isTimeout(null)).isFalse();
    }

    @Test
    void should_not_loop_forever_on_a_self_referencing_cause() {
        RuntimeException exception = new RuntimeException("boom") {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };
        assertThat(SpringRestClient.isTimeout(exception)).isFalse();
    }
}
