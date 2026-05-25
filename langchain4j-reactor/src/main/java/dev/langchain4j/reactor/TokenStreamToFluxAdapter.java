package dev.langchain4j.reactor;

import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.spi.services.TokenStreamAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TokenStreamToFluxAdapter implements TokenStreamAdapter {

    @Override
    public boolean canAdaptTokenStreamTo(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == Flux.class) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                return typeArguments.length == 1 && typeArguments[0] == String.class;
            }
        }
        return false;
    }

    @Override
    public Object adapt(TokenStream tokenStream) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
        AtomicBoolean cancelled = new AtomicBoolean();
        AtomicReference<StreamingHandle> streamingHandleReference = new AtomicReference<>();
        Set<StreamingHandle> streamingHandlesWithCancellationAttempts =
                Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

        Flux<String> flux = sink.asFlux().doOnCancel(() -> {
            cancelled.set(true);
            cancel(streamingHandleReference.get(), streamingHandlesWithCancellationAttempts);
        });

        tokenStream.onStreamingHandle(streamingHandle -> {
                    streamingHandleReference.set(streamingHandle);
                    if (cancelled.get()) {
                        cancel(streamingHandle, streamingHandlesWithCancellationAttempts);
                    }
                })
                .onPartialResponse(sink::tryEmitNext)
                .onCompleteResponse(ignored -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();
        return flux;
    }

    private static void cancel(
            StreamingHandle streamingHandle, Set<StreamingHandle> streamingHandlesWithCancellationAttempts) {
        if (streamingHandle == null || streamingHandle.isCancelled()) {
            return;
        }
        if (!streamingHandlesWithCancellationAttempts.add(streamingHandle)) {
            return;
        }
        try {
            streamingHandle.cancel();
        } catch (RuntimeException ignored) {
            // Some StreamingChatModel implementations expose non-cancellable handles.
        }
    }
}
