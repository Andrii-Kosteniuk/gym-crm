package com.training.gym.service;

import com.training.gym.config.security.bruteforce.LoginAttemptService;
import com.training.gym.config.security.jwt.JwtRevocationService;
import com.training.gym.config.security.jwt.JwtTokenService;
import com.training.gym.exception.AccountTemporarilyLockedException;
import com.training.gym.service.auth.AuthenticationServiceImpl;
import com.training.gym.service.trainee.TraineeService;
import com.training.gym.service.trainer.TrainerService;
import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.mapper.TraineeWebMapper;
import com.training.gym.web.mapper.TrainerWebMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    TraineeService traineeService;
    @Mock
    TrainerService trainerService;
    @Mock
    JwtTokenService jwtTokenService;
    @Mock
    LoginAttemptService loginAttemptService;
    @Mock
    TraineeWebMapper traineeWebMapper;
    @Mock
    TrainerWebMapper trainerWebMapper;
    @Mock
    JwtRevocationService revocationService;
    @InjectMocks
    AuthenticationServiceImpl service;
    private final AuthRequest request = new AuthRequest("owner", "password");

    @Test
    void shouldAuthenticateAndReturnToken() {
        // Arrange
        when(loginAttemptService.getRemainingLockDuration("owner")).thenReturn(Duration.ZERO);
        when(authenticationManager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated("owner", null, List.of()));
        when(jwtTokenService.generateToken("owner")).thenReturn("token");
        // Act
        var response = service.authenticate(request);
        // Assert
        assertEquals("token", response.accessToken());
        verify(authenticationManager).authenticate(UsernamePasswordAuthenticationToken.unauthenticated("owner", "password"));
    }

    @Test
    void shouldRejectInvalidCredentials() {
        // Arrange
        when(loginAttemptService.getRemainingLockDuration("owner")).thenReturn(Duration.ZERO);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));
        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> service.authenticate(request));
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldRejectBlockedUserBeforeAuthentication() {
        // Arrange
        when(loginAttemptService.getRemainingLockDuration("owner")).thenReturn(Duration.ofMinutes(5));
        // Act & Assert
        assertThrows(AccountTemporarilyLockedException.class, () -> service.authenticate(request));
        verifyNoInteractions(authenticationManager, jwtTokenService);
    }

    @Test
    void shouldRejectThirdFailureWithLockException() {
        // Arrange
        when(loginAttemptService.getRemainingLockDuration("owner")).thenReturn(Duration.ZERO, Duration.ofMinutes(5));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));
        when(loginAttemptService.isBlocked("owner")).thenReturn(true);
        // Act & Assert
        assertThrows(AccountTemporarilyLockedException.class, () -> service.authenticate(request));
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        Instant expires = Instant.now().plusSeconds(900);
        when(jwt.getId()).thenReturn("id");
        when(jwt.getExpiresAt()).thenReturn(expires);
        // Act
        service.logout(jwt);
        // Assert
        verify(revocationService).revoke("id", expires);
    }
}
