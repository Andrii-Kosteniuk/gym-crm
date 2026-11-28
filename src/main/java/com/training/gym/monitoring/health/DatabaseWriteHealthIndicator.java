package com.training.gym.monitoring.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("databaseWrite")
@RequiredArgsConstructor
public class DatabaseWriteHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public @Nullable Health health() {
        try {
            String readOnly = jdbcTemplate.queryForObject(
                    "SHOW transaction_read_only", String.class
            );

            if ("off".equalsIgnoreCase(readOnly)) {
                return Health.up()
                        .withDetail("readOnly", false)
                        .build();
            }

            return Health.down()
                    .withDetail("readOnly", true)
                    .build();

        } catch (RuntimeException ex) {
            log.warn("Database write health check failed", ex);

            return Health.down().build();
        }
    }
}
