package com.training.gym.service.auth;

import com.training.gym.config.security.jwt.JwtTokenService;
import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {

        log.debug("Authentication attempt: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(),
                        request.password()
                )
        );

        log.debug("Authentication successful: {}", authentication.getName());

        String token = jwtTokenService.generateToken(authentication.getName());

        log.debug("JWT successfully generated");

        return new AuthResponse(token);
    }
}
