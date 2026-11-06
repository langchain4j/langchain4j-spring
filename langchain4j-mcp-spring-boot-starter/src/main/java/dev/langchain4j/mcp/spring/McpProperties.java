package dev.langchain4j.mcp.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("langchain4j.mcp")
public class McpProperties {

    private boolean enabled = true;
    private Map<String, Client> clients = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Client> getClients() {
        return clients;
    }

    public void setClients(Map<String, Client> clients) {
        this.clients = clients;
    }

    public static class Client {

        private Transport transport;
        private String url;
        private List<String> command = new ArrayList<>();
        private Map<String, String> environment = new LinkedHashMap<>();
        private Map<String, String> headers = new LinkedHashMap<>();

        public Transport getTransport() {
            return transport;
        }

        public void setTransport(Transport transport) {
            this.transport = transport;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getCommand() {
            return command;
        }

        public void setCommand(List<String> command) {
            this.command = command;
        }

        public Map<String, String> getEnvironment() {
            return environment;
        }

        public void setEnvironment(Map<String, String> environment) {
            this.environment = environment;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }

    public enum Transport {
        HTTP,
        STDIO
    }
}
