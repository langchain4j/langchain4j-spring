package dev.langchain4j.store.embedding.pgvector.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = PgVectorDataSourceProperties.PREFIX)
public record PgVectorDataSourceProperties(
        boolean enabled,
        String host,
        String user,
        String password,
        Integer port,
        String database
) {
    static final String PREFIX = "langchain4j.pgvector.datasource";

    /**
     * Provide a default constructor that sets the default value of enabled to false.
     */
    public PgVectorDataSourceProperties() {
        this(false, null, null, null, null, null);
    }
}
