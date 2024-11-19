    package dev.langchain4j.googleaigemini.spring;


    import dev.langchain4j.model.chat.request.ResponseFormat;

    import java.time.Duration;

    public class ChatModelProperties {

        private String modelName;
        private double temperature;
        private double topP;
        private Integer topK;
        private Integer maxOutputTokens;
        private ResponseFormat responseFormat;
        private boolean logRequestsAndResponses;
        private Integer maxRetries;

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

        private Duration timeout;

        public boolean isLogRequestsAndResponses() {
            return logRequestsAndResponses;
        }

        public void setLogRequestsAndResponses(boolean logRequestsAndResponses) {
            this.logRequestsAndResponses = logRequestsAndResponses;
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

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getTopP() {
            return topP;
        }

        public void setTopP(double topP) {
            this.topP = topP;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public Integer getMaxOutputTokens() {
            return maxOutputTokens;
        }

        public void setMaxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
        }

    }
