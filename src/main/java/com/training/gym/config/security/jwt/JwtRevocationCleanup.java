package com.training.gym.config.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRevocationCleanup {

    private final JwtRevocationService revocationService;

    @Scheduled(fixedDelay = 60_000)
    public void cleanup() {
        revocationService.removeExpiredTokens();
    }
}
