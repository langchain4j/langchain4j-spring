package dev.langchain4j.azure.aisearch.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.azure.ai-search";

    String endpoint;
    String apiKey;
    int dimensions;
    boolean createOrUpdateIndex;
}
