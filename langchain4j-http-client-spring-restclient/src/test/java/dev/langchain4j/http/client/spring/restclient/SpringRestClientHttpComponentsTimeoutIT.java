package dev.langchain4j.http.client.spring.restclient;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import java.net.SocketTimeoutException;

class SpringRestClientHttpComponentsTimeoutIT extends AbstractSpringRestClientTimeoutIT {

    @Override
    protected ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder() {
        return ClientHttpRequestFactoryBuilder.httpComponents();
    }

    @Override
    protected Class<? extends Exception> expectedReadTimeoutRootCauseExceptionType() {
        return SocketTimeoutException.class;
    }
}
