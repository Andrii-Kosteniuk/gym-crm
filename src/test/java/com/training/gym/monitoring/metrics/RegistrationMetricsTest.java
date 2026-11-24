package com.training.gym.monitoring.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationMetricsTest {

    private static final String METRIC_NAME = "gym.users.registrations";

    private static final String ROLE = "role";
    private static final String TRAINEE = "trainee";
    private static final String TRAINER = "trainer";

    private SimpleMeterRegistry registry;
    private RegistrationMetrics registrationMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        registrationMetrics = new RegistrationMetrics(registry);
    }

    @AfterEach
    void tearDown() {
        registry.close();
    }

    @Test
    void shouldInitializeRegistrationCountersWithZeroValues() {

        // Arrange & Act

        // Assert
        assertAll(
                () -> assertEquals(0.0, registrationCount(TRAINEE)),
                () -> assertEquals(0.0, registrationCount(TRAINER))
        );
    }

    @Test
    void shouldIncrementTraineeRegistrationCounter() {

        // Arrange

        // Act
        registrationMetrics.recordTraineeRegistration();

        // Assert
        assertAll(
                () -> assertEquals(1.0, registrationCount(TRAINEE)),
                () -> assertEquals(0.0, registrationCount(TRAINER))
        );
    }

    @Test
    void shouldIncrementTrainerRegistrationCounter() {

        // Arrange

        // Act
        registrationMetrics.recordTrainerRegistration();

        // Assert
        assertAll(
                () -> assertEquals(0.0, registrationCount(TRAINEE)),
                () -> assertEquals(1.0, registrationCount(TRAINER))
        );
    }

    @Test
    void shouldAccumulateMultipleTraineeRegistrations() {

        // Arrange

        // Act
        registrationMetrics.recordTraineeRegistration();
        registrationMetrics.recordTraineeRegistration();
        registrationMetrics.recordTraineeRegistration();

        // Assert
        assertAll(
                () -> assertEquals(3.0, registrationCount(TRAINEE)),
                () -> assertEquals(0.0, registrationCount(TRAINER))
        );
    }

    @Test
    void shouldAccumulateMultipleTrainerRegistrations() {

        // Arrange

        // Act
        registrationMetrics.recordTrainerRegistration();
        registrationMetrics.recordTrainerRegistration();

        // Assert
        assertAll(
                () -> assertEquals(0.0, registrationCount(TRAINEE)),
                () -> assertEquals(2.0, registrationCount(TRAINER))
        );
    }

    @Test
    void shouldTrackTrainerAndTraineeRegistrationsIndependently() {

        // Arrange

        // Act
        registrationMetrics.recordTraineeRegistration();
        registrationMetrics.recordTrainerRegistration();
        registrationMetrics.recordTraineeRegistration();

        // Assert
        assertAll(
                () -> assertEquals(2.0, registrationCount(TRAINEE)),
                () -> assertEquals(1.0, registrationCount(TRAINER))
        );
    }

    @Test
    void shouldRegisterCountersWithCorrectDescription() {

        // Arrange
        String expectedDescription =
                "Number of successful user registrations";

        // Act
        var traineeCounter = registry.get(METRIC_NAME)
                .tag(ROLE, TRAINEE)
                .counter();

        var trainerCounter = registry.get(METRIC_NAME)
                .tag(ROLE, TRAINER)
                .counter();

        // Assert
        assertAll(
                () -> assertEquals(
                        expectedDescription,
                        traineeCounter.getId().getDescription()
                ),
                () -> assertEquals(
                        expectedDescription,
                        trainerCounter.getId().getDescription()
                )
        );
    }

    private double registrationCount(String role) {
        return registry.get(METRIC_NAME)
                .tag(ROLE, role)
                .counter()
                .count();
    }
}