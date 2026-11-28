package com.training.gym.monitoring.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveUserMetrics {

    private static final String ACTIVE_TRAINEES_QUERY = """
            SELECT COUNT(*)
            FROM trainees t
            JOIN users u ON t.id = u.id
            WHERE u.active = true
            """;

    private static final String ACTIVE_TRAINERS_QUERY = """
            SELECT COUNT(*)
            FROM trainers t
            JOIN users u ON t.id = u.id
            WHERE u.active = true
            """;

    public ActiveUserMetrics(MeterRegistry registry, JdbcTemplate jdbcTemplate) {

        Gauge.builder("gym.trainees.active",
                jdbcTemplate,
                jdbc -> countUsers(jdbc, ACTIVE_TRAINEES_QUERY))
                .description("Current number of active trainees")
                .register(registry);

        Gauge.builder("gym.trainers.active",
                jdbcTemplate, jdbc -> countUsers(jdbc, ACTIVE_TRAINERS_QUERY))
                .description("Current number of active trainers").
                register(registry);
    }

    private double countUsers(JdbcTemplate jdbcTemplate, String query) {
        Long count = jdbcTemplate.queryForObject(query, Long.class);

        return count != null ? count.doubleValue() : Double.NaN;
    }
}