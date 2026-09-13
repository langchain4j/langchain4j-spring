package dev.langchain4j.openai.spring;

import java.net.InetSocketAddress;
import java.net.Proxy;

record ProxyProperties(
        Proxy.Type type,
        String host,
        Integer port
) {

    static Proxy toProxy(ProxyProperties properties) {
        if (properties == null || properties.type() == null || properties.type() == Proxy.Type.DIRECT) {
            return null;
        }

        if (properties.host() == null || properties.port() == null) {
            throw new IllegalArgumentException("Proxy host and port must be configured");
        }

        return new Proxy(properties.type(), new InetSocketAddress(properties.host(), properties.port()));
    }
}
