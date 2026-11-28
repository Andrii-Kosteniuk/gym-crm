package com.training.gym.config.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JwtRevocationService {

    private static final Duration DURATION_OF_SECONDS = Duration.ofSeconds(60);

    private final Clock clock;

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {

        Objects.requireNonNull(tokenId, "Token ID is required");
        Objects.requireNonNull(expiresAt, "Expiration is required");

        Instant retainUntil = expiresAt.plus(DURATION_OF_SECONDS);

        if (clock.instant().isBefore(retainUntil)) {

            revokedTokens.merge(
                    tokenId,
                    retainUntil,
                    (current, candidate) ->
                            current.isAfter(candidate)
                                    ? current
                                    : candidate
            );
        }
    }

    public boolean isRevoked(String tokenId) {

        Instant now = clock.instant();

        Instant retainUntil = revokedTokens.computeIfPresent(
                tokenId,
                (key, expiration) ->
                        now.isBefore(expiration)
                                ? expiration
                                : null
        );

        return retainUntil != null;
    }

    public void removeExpiredTokens() {

        Instant now = clock.instant();

        revokedTokens.entrySet().removeIf(
                entry -> now.isAfter(entry.getValue())
        );
    }
}