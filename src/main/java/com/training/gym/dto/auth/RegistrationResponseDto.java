package com.training.gym.dto.auth;

public record RegistrationResponseDto(

        Long id,
        String username,
        String password
) {
}
