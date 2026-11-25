package dev.langchain4j.mcp.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(McpProperties.PREFIX)
public class McpProperties {

    static final String PREFIX = "langchain4j.mcp";

    private boolean enabled = true;
    private Map<String, Client> clients = new LinkedHashMap<>();
    private ToolProvider toolProvider = new ToolProvider();

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

    public ToolProvider getToolProvider() {
        return toolProvider;
    }

    public void setToolProvider(ToolProvider toolProvider) {
        this.toolProvider = toolProvider;
    }

    public static class ToolProvider {

        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Client {

        private Transport transport;
        private String url;
        private List<String> command = new ArrayList<>();
        private Map<String, String> environment = new LinkedHashMap<>();
        private Map<String, String> customHeaders = new LinkedHashMap<>();
        private String protocolVersion;
        private Duration initializationTimeout;
        private Duration toolExecutionTimeout;
        private Boolean logRequests;
        private Boolean logResponses;
        private Boolean logEvents;

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

        public Map<String, String> getCustomHeaders() {
            return customHeaders;
        }

        public void setCustomHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public Duration getInitializationTimeout() {
            return initializationTimeout;
        }

        public void setInitializationTimeout(Duration initializationTimeout) {
            this.initializationTimeout = initializationTimeout;
        }

        public Duration getToolExecutionTimeout() {
            return toolExecutionTimeout;
        }

        public void setToolExecutionTimeout(Duration toolExecutionTimeout) {
            this.toolExecutionTimeout = toolExecutionTimeout;
        }

        public Boolean getLogRequests() {
            return logRequests;
        }

        public void setLogRequests(Boolean logRequests) {
            this.logRequests = logRequests;
        }

        public Boolean getLogResponses() {
            return logResponses;
        }

        public void setLogResponses(Boolean logResponses) {
            this.logResponses = logResponses;
        }

        public Boolean getLogEvents() {
            return logEvents;
        }

        public void setLogEvents(Boolean logEvents) {
            this.logEvents = logEvents;
        }
    }

    public enum Transport {
        STREAMABLE_HTTP,
        STDIO
    }
}
