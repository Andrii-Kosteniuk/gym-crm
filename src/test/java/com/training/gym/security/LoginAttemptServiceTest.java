package com.training.gym.security;

import com.training.gym.config.security.bruteforce.LoginAttemptProperties;
import com.training.gym.config.security.bruteforce.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginAttemptServiceTest {
    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private final Clock clock = mock(Clock.class);
    private final LoginAttemptService service = new LoginAttemptService(new LoginAttemptProperties(3, Duration.ofMinutes(5)), clock);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(now);
    }

    @Test
    void shouldNotBlockAfterTwoFailures() {
        // Arrange
        service.recordFailure("user");
        // Act
        service.recordFailure("user");
        // Assert
        assertFalse(service.isBlocked("user"));
    }

    @Test
    void shouldBlockAfterThreeFailures() {
        // Arrange
        service.recordFailure("user");
        service.recordFailure("user");
        // Act
        service.recordFailure("user");
        // Assert
        assertTrue(service.isBlocked("user"));
        assertEquals(Duration.ofMinutes(5), service.getRemainingLockDuration("user"));
    }

    @Test
    void shouldUnlockAfterFiveMinutes() {
        // Arrange
        for (int i = 0; i < 3; i++) service.recordFailure("user");
        when(clock.instant()).thenReturn(now.plusSeconds(300));
        // Act
        boolean blocked = service.isBlocked("user");
        // Assert
        assertFalse(blocked);
    }

    @Test
    void shouldResetAttemptsAfterSuccessfulLogin() {
        // Arrange
        service.recordFailure("user");
        service.recordFailure("user");
        // Act
        service.recordSuccess("user");
        service.recordFailure("user");
        // Assert
        assertFalse(service.isBlocked("user"));
    }

    @Test
    void shouldNotExtendExistingLock() {
        // Arrange
        for (int i = 0; i < 3; i++) service.recordFailure("user");
        when(clock.instant()).thenReturn(now.plusSeconds(60));
        // Act
        service.recordFailure("user");
        // Assert
        assertEquals(Duration.ofMinutes(4), service.getRemainingLockDuration("user"));
    }
}
