    package dev.langchain4j.googleaigemini.spring;

    import dev.langchain4j.model.chat.request.ResponseFormat;
    import org.springframework.boot.context.properties.NestedConfigurationProperty;

    import java.time.Duration;

    public class ChatModelProperties {

        private String modelName;
        private Double temperature;
        private Double topP;
        private Integer topK;
        private Integer maxOutputTokens;
        private ResponseFormat responseFormat;
        private Boolean logRequestsAndResponses;
        private Integer maxRetries;
        private Duration timeout;

        private GeminiSafetySetting safetySetting;

        private GeminiFunctionCallingConfig functionCallingConfig;

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public ResponseFormat getResponseFormat() {
            return responseFormat;
        }

        public void setResponseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public Boolean getLogRequestsAndResponses() {
            return logRequestsAndResponses;
        }

        public void setLogRequestsAndResponses(Boolean logRequestsAndResponses) {
            this.logRequestsAndResponses = logRequestsAndResponses;
        }

        public GeminiSafetySetting getSafetySetting() {
            return safetySetting;
        }

        public void setSafetySetting(GeminiSafetySetting safetySetting) {
            this.safetySetting = safetySetting;
        }

        public GeminiFunctionCallingConfig getFunctionCallingConfig() {
            return functionCallingConfig;
        }

        public void setFunctionCallingConfig(GeminiFunctionCallingConfig functionCallingConfig) {
            this.functionCallingConfig = functionCallingConfig;
        }

        public Integer getMaxOutputTokens() {
            return maxOutputTokens;
        }

        public void setMaxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
        }

    }
