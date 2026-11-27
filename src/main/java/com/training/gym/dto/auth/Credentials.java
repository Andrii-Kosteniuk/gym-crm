package com.training.gym.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials used to authenticate a user")
public record Credentials (

        @Schema(description = "Account username")
        @NotBlank String username,

        @Schema(description = "Password")
        @NotBlank String password
) {
}
