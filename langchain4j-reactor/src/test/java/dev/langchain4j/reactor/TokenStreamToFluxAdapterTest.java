package dev.langchain4j.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

class TokenStreamToFluxAdapterTest {

    interface Assistant {

        Flux<String> fluxOfString();

        Flux flux();

        Flux<Object> fluxOfObject();
    }

    @Test
    void test_canAdapt() {

        TokenStreamToFluxAdapter adapter = new TokenStreamToFluxAdapter();

        assertThat(adapter.canAdaptTokenStreamTo(getReturnTypeOfMethod("fluxOfString"))).isTrue();

        assertThat(adapter.canAdaptTokenStreamTo(getReturnTypeOfMethod("flux"))).isFalse();
        assertThat(adapter.canAdaptTokenStreamTo(getReturnTypeOfMethod("fluxOfObject"))).isFalse();
    }

    private static Type getReturnTypeOfMethod(String methodName) {
        try {
            return Assistant.class.getDeclaredMethod(methodName).getGenericReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}