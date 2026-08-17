package dev.langchain4j.http.client.spring.restclient;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import io.netty.handler.timeout.ReadTimeoutException;

class SpringRestClientReactorTimeoutIT extends AbstractSpringRestClientTimeoutIT {

    @Override
    protected ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder() {
        return ClientHttpRequestFactoryBuilder.reactor();
    }

    @Override
    protected Class<? extends Exception> expectedReadTimeoutRootCauseExceptionType() {
        return ReadTimeoutException.class;
    }

    @Override
    protected String[] readAsyncMessageKeywords() {
        return new String[] {"I/O error"}; // Netty's ReadTimeoutException carries no message of its own
    }
}
