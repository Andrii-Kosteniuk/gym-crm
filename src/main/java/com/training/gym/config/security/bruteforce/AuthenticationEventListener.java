package com.training.gym.config.security.bruteforce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {

        Authentication authentication = event.getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken) {

            String username = authentication.getName();

            log.warn("Failed authentication for user: {}", username);

            loginAttemptService.recordFailure(username);
        }
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {

        Authentication authentication = event.getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken) {

            String username = authentication.getName();

            log.debug("Successful authentication for user: {}", username);

            loginAttemptService.recordSuccess(username);
        }
    }

    @EventListener
    public void onAccountLocked(AuthenticationFailureLockedEvent event) {

        log.warn("Authentication rejected: account locked for user: {}",
                event.getAuthentication().getName());
    }
}