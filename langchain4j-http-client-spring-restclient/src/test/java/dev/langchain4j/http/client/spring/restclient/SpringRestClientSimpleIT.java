package dev.langchain4j.http.client.spring.restclient;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;

/**
 * Runs in the nightly build only, see {@code .github/workflows/nightly.yaml}.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class SpringRestClientSimpleIT extends AbstractSpringRestClientIT {

    @Override
    protected ClientHttpRequestFactoryBuilder<?> clientHttpRequestFactoryBuilder() {
        return ClientHttpRequestFactoryBuilder.simple();
    }

    @Override
    protected boolean supportsErrorBodyOnUnauthorized() {
        // SimpleClientHttpRequestFactory always streams the request body, and HttpURLConnection cannot replay a
        // streamed body to retry a 401 with credentials, so it throws HttpRetryException instead of handing over
        // the response body it already received. Spring is then left with an empty body. Other status codes are
        // unaffected, and there is no way to turn this off: SimpleClientHttpRequestFactory.setBufferRequestBody()
        // is deprecated in Spring Framework 6.2 and gone in 7.0.
        return false;
    }
}
