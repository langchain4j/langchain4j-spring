package dev.langchain4j.http.client.spring.restclient;

import dev.langchain4j.exception.AsyncNotSupportedException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.stream.Stream;

import static dev.langchain4j.http.client.HttpMethod.GET;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringRestClientWithoutWebfluxTest {

    private static final String[] WEBFLUX_PACKAGES = {"org.springframework.web.reactive.", "reactor."};

    private static final HttpRequest REQUEST = HttpRequest.builder()
            .method(GET)
            .url("http://localhost:1/never-called")
            .build();

    @Test
    void public_classes_should_not_expose_webflux_types() {
        // spring-webflux is optional. Spring introspects the declared methods and fields of a bean's class (for
        // example SpringRestClientBuilder, which the starters declare as beans), and that loads every type they
        // mention: a webflux type here would make every application without spring-webflux fail to start.
        Stream.of(SpringRestClient.class, SpringRestClientBuilder.class, SpringRestClientBuilderFactory.class)
                .forEach(type -> assertThat(signatureTypes(type))
                        .as("types in the signatures of %s", type.getSimpleName())
                        .noneMatch(SpringRestClientWithoutWebfluxTest::isWebfluxType));
    }

    @Test
    void should_fail_non_blocking_calls_with_a_message_saying_what_to_add() throws Exception {

        // given: SpringRestClient loaded by a class loader that cannot see spring-webflux or Reactor
        HttpClient client = newClientWithoutWebflux();

        // when
        CompletableFuture<?> future = client.executeAsync(REQUEST);

        // then
        assertThatThrownBy(() -> future.get(5, SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isExactlyInstanceOf(AsyncNotSupportedException.class)
                .hasMessageContaining("org.springframework.boot:spring-boot-starter-webclient");

        // when
        CompletableFuture<Throwable> streamError = new CompletableFuture<>();
        client.stream(REQUEST).subscribe(new Flow.Subscriber<HttpStreamingEvent>() {

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(HttpStreamingEvent event) {
                streamError.completeExceptionally(new AssertionError("no event expected: " + event));
            }

            @Override
            public void onError(Throwable throwable) {
                streamError.complete(throwable);
            }

            @Override
            public void onComplete() {
                streamError.completeExceptionally(new AssertionError("error expected"));
            }
        });

        // then
        assertThat(streamError.get(5, SECONDS))
                .isExactlyInstanceOf(AsyncNotSupportedException.class)
                .hasMessageContaining("org.springframework.boot:spring-boot-starter-webclient");
    }

    @Test
    void should_not_accept_null_web_client_builder() {
        assertThatThrownBy(() -> WebClientBuilderHolder.of(null))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("webClientBuilder");
    }

    private static HttpClient newClientWithoutWebflux() throws Exception {
        ClassLoader classLoader = new WithoutWebfluxClassLoader(SpringRestClientWithoutWebfluxTest.class.getClassLoader());
        Class<?> clientClass = classLoader.loadClass(SpringRestClient.class.getName());
        Object builder = clientClass.getMethod("builder").invoke(null);
        return (HttpClient) builder.getClass().getMethod("build").invoke(builder);
    }

    private static boolean isWebfluxType(String className) {
        return Arrays.stream(WEBFLUX_PACKAGES).anyMatch(className::startsWith);
    }

    private static Stream<String> signatureTypes(Class<?> type) {
        Stream<Class<?>> methodTypes = Arrays.stream(type.getDeclaredMethods())
                .flatMap(method -> Stream.concat(Stream.of(method.getReturnType()), Arrays.stream(method.getParameterTypes())));
        Stream<Class<?>> fieldTypes = Arrays.stream(type.getDeclaredFields()).map(Field::getType);
        return Stream.concat(methodTypes, fieldTypes).map(Class::getName);
    }

    /**
     * Hides spring-webflux and Reactor, and defines the classes of this module itself, so that they see the
     * classpath of an application without spring-webflux. Everything else is delegated to the parent.
     */
    private static class WithoutWebfluxClassLoader extends ClassLoader {

        private static final String OWN_PACKAGE = SpringRestClient.class.getPackageName() + ".";

        WithoutWebfluxClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                if (isWebfluxType(name)) {
                    throw new ClassNotFoundException(name);
                }
                if (!name.startsWith(OWN_PACKAGE)) {
                    return super.loadClass(name, resolve);
                }
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    byte[] bytes = readClassFile(name);
                    loaded = defineClass(name, bytes, 0, bytes.length);
                }
                if (resolve) {
                    resolveClass(loaded);
                }
                return loaded;
            }
        }

        private byte[] readClassFile(String name) throws ClassNotFoundException {
            try (InputStream inputStream = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                if (inputStream == null) {
                    throw new ClassNotFoundException(name);
                }
                return inputStream.readAllBytes();
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }
}
