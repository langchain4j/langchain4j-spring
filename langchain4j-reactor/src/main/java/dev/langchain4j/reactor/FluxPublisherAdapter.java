package dev.langchain4j.reactor;

import dev.langchain4j.service.AiServiceStreamingEvent;
import dev.langchain4j.spi.services.PublisherAdapter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;
import org.reactivestreams.FlowAdapters;
import reactor.core.publisher.Flux;

/**
 * Adapts the {@link Flow.Publisher} produced by a non-blocking streaming AI Service to a Reactor {@link Flux}, so
 * an AI Service method can be declared to return {@code Flux<AiServiceStreamingEvent>}.
 * <p>
 * <b>{@code Flux<String>} is deliberately not handled here.</b> It is already served by
 * {@link TokenStreamToFluxAdapter} over the callback-based {@code TokenStream}, which works with every provider.
 * The non-blocking path only works with providers that implement the reactive chat SPI, and {@code AiServices}
 * checks for a {@link PublisherAdapter} before it checks for a {@code TokenStream} adapter — so claiming
 * {@code Flux<String>} here would silently move every existing {@code Flux<String>} method onto the non-blocking
 * path and break it on every provider that has not opted in.
 *
 * @see TokenStreamToFluxAdapter
 */
public class FluxPublisherAdapter implements PublisherAdapter {

    @Override
    public boolean canAdapt(Type type) {
        return type instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() == Flux.class
                && parameterizedType.getActualTypeArguments().length == 1
                && parameterizedType.getActualTypeArguments()[0] == AiServiceStreamingEvent.class;
    }

    @Override
    public Object fromPublisher(Type type, Flow.Publisher<?> publisher) {
        return Flux.from(FlowAdapters.toPublisher(publisher));
    }
}
