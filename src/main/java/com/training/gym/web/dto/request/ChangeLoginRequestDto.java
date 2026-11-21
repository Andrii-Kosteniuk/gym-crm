package com.training.gym.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeLoginRequestDto(
        @NotBlank String username,
        @NotBlank
        String oldPassword,
        @NotBlank String newPassword
) {


}
