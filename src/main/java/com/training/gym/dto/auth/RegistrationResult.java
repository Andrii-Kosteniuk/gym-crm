package com.training.gym.dto.auth;

public record RegistrationResult<T>(
        T profile,
        String initialPassword
) {
}