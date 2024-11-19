package dev.langchain4j.store.embedding.pgvector.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = PgVectorDataSourceProperties.PREFIX)
public class PgVectorDataSourceProperties {

    static final String PREFIX = "langchain4j.pgvector.datasource";

    /**
     * Enable postgres datasource configuration, default value <code>false</code>.
     */
    private boolean enabled = false;

    /**
     * The pgvector database host.
     */
    private String host;

    /**
     * The pgvector database user.
     */
    private String user;

    /**
     * The pgvector database password.
     */
    private String password;

    /**
     * The pgvector database port.
     */
    private Integer port;

    /**
     * The pgvector database name.
     */
    private String database;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
