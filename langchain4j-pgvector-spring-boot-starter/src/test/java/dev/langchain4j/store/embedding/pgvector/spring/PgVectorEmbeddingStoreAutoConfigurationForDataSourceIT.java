package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PgVectorEmbeddingStoreAutoConfigurationForDataSourceIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PgVectorEmbeddingStoreAutoConfiguration.class))
            .withPropertyValues(
                    "langchain4j.pgvector.enabled=true",
                    "langchain4j.pgvector.datasource.enabled=false",
                    "langchain4j.pgvector.datasource.host=localhost",
                    "langchain4j.pgvector.datasource.port=5432",
                    "langchain4j.pgvector.datasource.user=testuser",
                    "langchain4j.pgvector.datasource.password=testpassword",
                    "langchain4j.pgvector.datasource.database=testdb",
                    "langchain4j.pgvector.table=embedding_table",
                    "langchain4j.pgvector.create-table=true",
                    "langchain4j.pgvector.dimension=768",
                    "langchain4j.pgvector.use-index=true",
                    "langchain4j.pgvector.index-list-size=10"
            );

    @Test
    void testAutoConfigurationWithExistingDataSource() {
        contextRunner
                .withUserConfiguration(ExistingDataSourceConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(PgVectorEmbeddingStore.class);

                    PgVectorEmbeddingStore store = context.getBean(PgVectorEmbeddingStore.class);
                    assertThat(store).isNotNull();

                    DataSource dataSource = context.getBean(DataSource.class);
                    assertThat(dataSource).isNotNull();
                });
    }

    @Test
    void testAutoConfigurationWithMultipleDataSourcesOfConfiguredTargetDataSourceBeanName() {
        contextRunner
                .withUserConfiguration(MultipleDataSourceConfig.class)
                .withPropertyValues("langchain4j.pgvector.datasource-bean-name=secondaryDataSource")
                .run(context -> {
                    // Verify that the PgVectorEmbeddingStore is correctly registered.
                    assertThat(context).hasSingleBean(PgVectorEmbeddingStore.class);

                    // Get PgVectorEmbeddingStore instance
                    PgVectorEmbeddingStore store = context.getBean(PgVectorEmbeddingStore.class);
                    assertThat(store).isNotNull();

                    // Get DataSource instance
                    DataSource secondaryDataSource = context.getBean("secondaryDataSource", DataSource.class);
                    assertThat(secondaryDataSource).isNotNull();

                    // Get the DataSource of the PgVectorEmbeddingStore using reflection.
                    DataSource storeDataSource = getDataSourceFromStore(store);

                    // Verify that the DataSource is consistent
                    assertThat(storeDataSource).isSameAs(secondaryDataSource);
                });
    }

    @Test
    void testAutoConfigurationWithMultipleDataSourcesOfNonConfiguredTargetDataSourceBeanName() {
        contextRunner
                .withUserConfiguration(MultipleDataSourceConfig.class)
                .run(context -> {
                    // Verify that the PgVectorEmbeddingStore is correctly registered.
                    assertThat(context).hasSingleBean(PgVectorEmbeddingStore.class);

                    // Get PgVectorEmbeddingStore instance
                    PgVectorEmbeddingStore store = context.getBean(PgVectorEmbeddingStore.class);
                    assertThat(store).isNotNull();

                    // Get DataSource instance
                    DataSource primaryDataSource = context.getBean("primaryDataSource", DataSource.class);
                    assertThat(primaryDataSource).isNotNull();

                    // Get the DataSource of the PgVectorEmbeddingStore using reflection.
                    DataSource storeDataSource = getDataSourceFromStore(store);

                    // Verify that the DataSource is consistent
                    assertThat(storeDataSource).isSameAs(primaryDataSource);
                });
    }

    private DataSource getDataSourceFromStore(PgVectorEmbeddingStore store) {
        try {
            // Let's assume that PgVectorEmbeddingStore has a field named “datasource” inside it.
            Field dataSourceField = PgVectorEmbeddingStore.class.getDeclaredField("datasource");
            dataSourceField.setAccessible(true);
            return (DataSource) dataSourceField.get(store);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access DataSource field from PgVectorEmbeddingStore", e);
        }
    }

    @Test
    void testAutoConfigurationWithoutPostgresDataSource() {
        contextRunner
                .withUserConfiguration(NonPostgresDataSourceConfig.class)
                .run(context -> {
                    // Verification context startup failure
                    Throwable startupFailure = context.getStartupFailure();
                    assertThat(startupFailure).isNotNull(); // Make sure there are startup failure exceptions
                    assertThat(startupFailure)
                            .isInstanceOf(BeanCreationException.class) // Validating Exception Types
                            .hasRootCauseInstanceOf(IllegalStateException.class) // Verification of root cause type
                            .hasMessageContaining("No suitable PostgreSQL DataSource found in the application context"); // 验证异常信息
                });
    }

    @Test
    void testAutoConfigurationDisabled() {
        contextRunner
                .withPropertyValues("langchain4j.pgvector.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(PgVectorEmbeddingStore.class));
    }


    private static DataSource mockPostgreDataSource() throws Exception {
        // Mock DataSource
        DataSource mockDataSource = mock(DataSource.class);

        // Mock Connection
        Connection mockConnection = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);

        // Mock DatabaseMetaData
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/testdb");

        // Mock PGConnection (PostgreSQL-specific connection)
        org.postgresql.PGConnection mockPGConnection = mock(org.postgresql.PGConnection.class);
        when(mockConnection.unwrap(org.postgresql.PGConnection.class)).thenReturn(mockPGConnection);

        // Mock PGConnection's addDataType method
        doNothing().when(mockPGConnection).addDataType(anyString(), any(Class.class));

        // Mock Statement
        Statement mockStatement = mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Mock SQL Execution (e.g., table creation or updates)
        when(mockStatement.executeUpdate(anyString())).thenReturn(1);

        return mockDataSource;
    }

    @Configuration
    static class ExistingDataSourceConfig {

        @Bean
        public DataSource dataSource() throws Exception {
            return mockPostgreDataSource();
        }
    }

    @Configuration
    static class MultipleDataSourceConfig {

        @Bean
        public DataSource primaryDataSource() throws Exception {
            return mockPostgreDataSource();
        }

        @Bean
        public DataSource secondaryDataSource() throws Exception {
            return mockPostgreDataSource();
        }
    }

    @Configuration
    static class NonPostgresDataSourceConfig {

        @Bean
        public DataSource dataSource() throws Exception {
            // Mock a non-PostgreSQL DataSource
            DataSource mockDataSource = mock(DataSource.class);
            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/testdb");

            return mockDataSource;
        }
    }
}
