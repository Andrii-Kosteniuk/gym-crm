package com.training.gym.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMetrics {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;

    public RegistrationMetrics(MeterRegistry registry) {

        traineeRegistrations = Counter.builder("gym.users.registrations")
                .description("Number of successful user registrations")
                .tag("role", "trainee")
                .register(registry);

        trainerRegistrations = Counter.builder("gym.users.registrations")
                .description("Number of successful user registrations")
                .tag("role", "trainer")
                .register(registry);
    }

    public void recordTraineeRegistration() {
        traineeRegistrations.increment();
    }

    public void recordTrainerRegistration() {
        trainerRegistrations.increment();
    }
}