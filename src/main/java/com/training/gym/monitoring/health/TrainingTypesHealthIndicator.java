package com.training.gym.monitoring.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("trainingTypes")
@RequiredArgsConstructor
@Slf4j
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbc;

    @Override
    public Health health() {
        try {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM training_types",
                    Integer.class
            );

            return count != null && count > 0
                    ? Health.up()
                      .withDetail("count", count)
                      .build()
                    : Health.down()
                      .withDetail("reason", "No training types configured")
                      .build();

        } catch (RuntimeException ex) {
            log.warn("Training type health check failed", ex);
            return Health.down().build();
        }
    }
}