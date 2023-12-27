package dev.langchain4j.store.embedding.elasticsearch.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Getter
@Setter
@ConditionalOnProperty(prefix = ElasticsearchProperties.PREFIX)
public class ElasticsearchProperties {

    static final String PREFIX = "langchain4j.elasticsearch";

    private String serverUrl;
    private String apiKey;
    private String userName;
    private String password;
    private String indexName = "default";
    private Integer dimension;
}
