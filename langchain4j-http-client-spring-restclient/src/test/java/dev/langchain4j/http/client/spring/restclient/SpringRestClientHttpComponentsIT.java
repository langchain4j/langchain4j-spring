package dev.langchain4j.http.client.spring.restclient;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

/**
 * Runs in the nightly build only, see {@code .github/workflows/nightly.yaml}.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class SpringRestClientHttpComponentsIT extends AbstractSpringRestClientIT {

    @Override
    protected ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder() {
        return ClientHttpRequestFactoryBuilder.httpComponents();
    }
}
