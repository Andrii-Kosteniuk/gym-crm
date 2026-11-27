package com.training.gym.monitoring.metrics;

import com.training.gym.exception.AuthenticationFailedException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationMetricsTest {

    private static final String AUTHENTICATION_METRIC = "gym.authentication.attempts";

    private static final String OUTCOME = "outcome";
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    @Mock
    private ProceedingJoinPoint joinPoint;

    private SimpleMeterRegistry registry;
    private AuthenticationMetrics authenticationMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        authenticationMetrics = new AuthenticationMetrics(registry);
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    @Test
    void shouldIncrementSuccessCounterWhenAuthenticationSucceeds() throws Throwable {

        // Arrange
        Object expectedResult = new Object();

        when(joinPoint.proceed()).thenReturn(expectedResult);

        // Act
        Object actualResult = authenticationMetrics.authentication(joinPoint);

        // Assert
        assertAll(
                () -> assertSame(expectedResult, actualResult),
                () -> assertEquals(1.0, authenticationCount(SUCCESS)),
                () -> assertEquals(0.0, authenticationCount(FAILURE))
        );

        verify(joinPoint).proceed();
    }

    @Test
    void shouldIncrementSuccessCounterWhenAuthenticationReturnsVoid() throws Throwable {

        // Arrange
        when(joinPoint.proceed()).thenReturn(null);

        // Act
        Object result = authenticationMetrics.authentication(joinPoint);

        // Assert
        assertAll(
                () -> assertNull(result),
                () -> assertEquals(1.0, authenticationCount(SUCCESS)),
                () -> assertEquals(0.0, authenticationCount(FAILURE))
        );

        verify(joinPoint).proceed();
    }

    @Test
    void shouldIncrementFailureCounterWhenAuthenticationFails() throws Throwable {

        // Arrange
        var exception = new AuthenticationFailedException("Authentication failed");

        when(joinPoint.proceed()).thenThrow(exception);

        // Act
        var thrown = assertThrows(
                AuthenticationFailedException.class,
                () -> authenticationMetrics.authentication(joinPoint)
        );

        // Assert
        assertAll(
                () -> assertSame(exception, thrown),
                () -> assertEquals(0.0, authenticationCount(SUCCESS)),
                () -> assertEquals(1.0, authenticationCount(FAILURE))
        );

        verify(joinPoint).proceed();
    }

    @Test
    void shouldPropagateUnexpectedExceptionWithoutIncrementingCounters()
            throws Throwable {

        // Arrange
        var exception = new IllegalStateException("Unexpected application error");

        when(joinPoint.proceed()).thenThrow(exception);

        // Act
        var thrown = assertThrows(
                IllegalStateException.class,
                () -> authenticationMetrics.authentication(joinPoint)
        );

        // Assert
        assertAll(
                () -> assertSame(exception, thrown),
                () -> assertEquals(0.0, authenticationCount(SUCCESS)),
                () -> assertEquals(0.0, authenticationCount(FAILURE))
        );

        verify(joinPoint).proceed();
    }

    @Test
    void shouldAccumulateSuccessfulAuthenticationAttempts() throws Throwable {

        // Arrange
        when(joinPoint.proceed()).thenReturn(null);

        // Act
        authenticationMetrics.authentication(joinPoint);
        authenticationMetrics.authentication(joinPoint);
        authenticationMetrics.authentication(joinPoint);

        // Assert
        assertAll(
                () -> assertEquals(3.0, authenticationCount(SUCCESS)),
                () -> assertEquals(0.0, authenticationCount(FAILURE))
        );

        verify(joinPoint, times(3)).proceed();
    }

    @Test
    void shouldTrackSuccessfulAndFailedAuthenticationIndependently() throws Throwable {

        // Arrange
        when(joinPoint.proceed())
                .thenReturn(null)
                .thenThrow(new AuthenticationFailedException("Authentication failed"))
                .thenReturn(null);

        // Act
        authenticationMetrics.authentication(joinPoint);

        assertThrows(AuthenticationFailedException.class, () -> authenticationMetrics.authentication(joinPoint));

        authenticationMetrics.authentication(joinPoint);

        // Assert
        assertAll(
                () -> assertEquals(2.0, authenticationCount(SUCCESS)),
                () -> assertEquals(1.0, authenticationCount(FAILURE))
        );

        verify(joinPoint, times(3)).proceed();
    }

    private double authenticationCount(String outcome) {
        return registry.get(AUTHENTICATION_METRIC)
                .tag(OUTCOME, outcome)
                .counter()
                .count();
    }

}