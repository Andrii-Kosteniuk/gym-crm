package com.training.gym.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/** Boots only the test infrastructure: no production PostgreSQL dependency. */
class TestPersistenceConfigurationTest {
    @Test
    void h2DataSourceAndFlywayStartWithTestProfile() throws Exception {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("test");
            context.register(ApplicationPropertiesConfig.class, DataSourceConfig.class, FlywayConfig.class);
            context.refresh();

            assertEquals(2, context.getBean(Flyway.class).info().applied().length);
            try (var connection = context.getBean(DataSource.class).getConnection();
                 var statement = connection.createStatement();
                 var rows = statement.executeQuery("SELECT COUNT(*) FROM training_types")) {
                assertTrue(rows.next());
                assertEquals(4, rows.getInt(1));
            }
        }
    }
}
