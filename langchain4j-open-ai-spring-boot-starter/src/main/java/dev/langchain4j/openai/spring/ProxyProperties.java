package dev.langchain4j.openai.spring;


import java.net.InetSocketAddress;
import java.net.Proxy;

record ProxyProperties(

    Proxy.Type type,
    String host,
    Integer port
){

    static Proxy convert(ProxyProperties proxy) {
        if (proxy == null) {
            return null;
        }
        return new Proxy(proxy.type, new InetSocketAddress(proxy.host, proxy.port));
    }
}
