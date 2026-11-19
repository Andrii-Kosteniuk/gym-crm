package com.training.gym.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationPropertiesConfigTest {
    @Test void testProfileLoadsH2Configuration() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("test");
            context.register(ApplicationPropertiesConfig.class);
            context.refresh();
            assertTrue(context.getEnvironment().getProperty("db.url").startsWith("jdbc:h2:mem:"));
            assertEquals("classpath:db/migration/h2", context.getEnvironment().getProperty("flyway.locations"));
            assertEquals("validate", context.getEnvironment().getProperty("hibernate.hbm2ddl.auto"));
        }
    }
    @Test void developmentProfileKeepsPostgresql() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(ApplicationPropertiesConfig.class);
            context.refresh();
            assertTrue(context.getEnvironment().getProperty("db.url").startsWith("jdbc:postgresql:"));
        }
    }
}
