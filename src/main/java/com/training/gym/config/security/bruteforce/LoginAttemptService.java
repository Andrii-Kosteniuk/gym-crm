package com.training.gym.config.security.bruteforce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final LoginAttemptProperties loginAttemptProperties;
    private final Clock clock;

    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void recordFailure(String username) {

        Instant now = clock.instant();

        LoginAttempt attempt = attempts.compute(username, (key, loginAttempt) -> {

            if (loginAttempt != null && loginAttempt.isLocked(now)) {
                return loginAttempt;
            }

            int failedAttempts = loginAttempt == null || loginAttempt.isExpired(now)
                    ? 1
                    : loginAttempt.failedAttempts() + 1;

            Instant lockedUntil = failedAttempts >= loginAttemptProperties.maxAttempts()
                    ? now.plus(loginAttemptProperties.lockDuration())
                    : null;

            return new LoginAttempt(failedAttempts, lockedUntil);

        });

        log.warn("Failed login: username={}, attempts={}, lockedUntil={}",
                username,
                attempt.failedAttempts(),
                attempt.lockedUntil());
    }

    public void recordSuccess(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {

        Instant now = clock.instant();

        LoginAttempt attempt = attempts.computeIfPresent(username, (key, loginAttempt) ->
                loginAttempt.isExpired(now)
                        ? null
                        : loginAttempt
        );

        return attempt != null && attempt.isLocked(now);
    }

    public Duration getRemainingLockDuration(String username) {

        Instant now = clock.instant();

        LoginAttempt attempt = attempts.get(username);

        if (attempt == null || !attempt.isLocked(now)) {
            return Duration.ZERO;
        }

        return Duration.between(now, attempt.lockedUntil());
    }
}