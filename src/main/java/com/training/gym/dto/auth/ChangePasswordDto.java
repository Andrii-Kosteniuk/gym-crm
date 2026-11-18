package com.training.gym.dto.auth;

public record ChangePasswordDto(
        String currentPassword,
        String newPassword
) {
}
