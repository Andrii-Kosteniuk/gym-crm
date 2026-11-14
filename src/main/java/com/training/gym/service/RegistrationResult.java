package com.training.gym.service;

public record RegistrationResult<T>(
        T profile,
        String initialPassword
) {
}