package com.training.gym.config.security.bruteforce;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "gym.security.brute-force")
public record LoginAttemptProperties(

        @Min(1)
        int maxAttempts,

        @NotNull
        Duration lockDuration
) {
}