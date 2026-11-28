package com.training.gym.config.security.bruteforce;

import java.time.Instant;

public record LoginAttempt (int failedAttempts,Instant lockedUntil)  {


    public boolean isLocked(Instant now) {
        return lockedUntil != null
                && now.isBefore(lockedUntil);
    }

    public boolean isExpired(Instant now) {
        return lockedUntil != null
                && !now.isBefore(lockedUntil);
    }
}
