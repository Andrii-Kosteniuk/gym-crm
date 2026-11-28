package com.training.gym.service.auth;

import com.training.gym.config.security.bruteforce.LoginAttemptService;
import com.training.gym.config.security.jwt.JwtTokenService;
import com.training.gym.exception.AccountTemporarilyLockedException;
import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {

        String username = request.username();
        String password = request.password();

        log.debug("Authentication attempt: {}", username);

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
        } catch (LockedException exception) {

            throw new AccountTemporarilyLockedException(
                    loginAttemptService.getRemainingLockDuration(username));

        } catch (BadCredentialsException exception) {

            if (loginAttemptService.isBlocked(username)) {

                throw new AccountTemporarilyLockedException(
                        loginAttemptService.getRemainingLockDuration(username));
            }

            throw exception;
        }

        log.debug("Authentication successful: {}", authentication.getName());

        String token = jwtTokenService.generateToken(authentication);

        log.debug("JWT successfully generated");

        return new AuthResponse(token);
    }
}
