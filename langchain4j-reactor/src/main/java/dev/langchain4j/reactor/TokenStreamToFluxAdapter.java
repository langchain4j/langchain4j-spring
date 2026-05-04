package dev.langchain4j.reactor;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.spi.services.TokenStreamAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        tokenStream.onPartialResponse(sink::tryEmitNext)
                .onCompleteResponse(ignored -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();
        return sink.asFlux();
    }
}
