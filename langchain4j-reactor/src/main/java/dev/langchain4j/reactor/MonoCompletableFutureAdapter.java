package dev.langchain4j.reactor;

import dev.langchain4j.spi.services.CompletableFutureAdapter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import reactor.core.publisher.Mono;

/**
 * Adapts between a {@link CompletableFuture} and a Reactor {@link Mono}, so an AI Service method - or a
 * {@code @Tool} method - can be declared to return {@code Mono<T>} and take part in the non-blocking path.
 * <p>
 * Subscribing to the returned {@link Mono} does not start the work: the AI Service call is already in flight by
 * the time the {@link Mono} exists, so this is a hot source. Cancelling the subscription cancels the underlying
 * future, which the AI Service treats as a request to stop the interaction.
 */
public class MonoCompletableFutureAdapter implements CompletableFutureAdapter {

    @Override
    public boolean canAdapt(Type type) {
        return type instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() == Mono.class;
    }

    @Override
    public CompletableFuture<?> toCompletableFuture(Object asyncValue) {
        return ((Mono<?>) asyncValue).toFuture();
    }

    @Override
    public Object fromCompletableFuture(Type type, CompletableFuture<?> future) {
        return Mono.fromFuture(() -> future).doOnCancel(() -> future.cancel(true));
    }
}
