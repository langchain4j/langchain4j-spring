package dev.langchain4j.http.client.spring.restclient;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

import java.net.http.HttpTimeoutException;

class SpringRestClientJdkTimeoutIT extends AbstractSpringRestClientTimeoutIT {

    @Override
    protected ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder() {
        return ClientHttpRequestFactoryBuilder.jdk();
    }

    @Override
    protected Class<? extends Exception> expectedReadTimeoutRootCauseExceptionType() {
        return HttpTimeoutException.class;
    }

    @Override
    protected String[] readAsyncMessageKeywords() {
        return new String[] {"I/O error"}; // the JDK HTTP client cancels the request, and the cancellation message is sometimes null
    }
}
