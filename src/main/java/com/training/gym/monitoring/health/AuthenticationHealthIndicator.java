package com.training.gym.monitoring.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("authentication")
@RequiredArgsConstructor
public class AuthenticationHealthIndicator implements HealthIndicator {

    private static final String TEST_PASSWORD = "health-check-password";

    private final PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Health health() {
        try {
            String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);

            if (!passwordEncoder.matches(TEST_PASSWORD, encodedPassword)) {
                return Health.down()
                        .withDetail("reason", "Password verification failed")
                        .build();
            }

            return Health.up()
                    .withDetail("passwordEncoder", passwordEncoder.getClass().getSimpleName())
                    .build();

        } catch (RuntimeException ex) {
            log.warn("Authentication health check failed", ex);

            return Health.down()
                    .withDetail("reason", "Password encoder unavailable")
                    .build();
        }
    }
}
