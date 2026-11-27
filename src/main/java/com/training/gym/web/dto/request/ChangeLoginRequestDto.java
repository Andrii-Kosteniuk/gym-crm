package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password change request")
public record ChangeLoginRequestDto(

        @Schema(description = "Account username")
        @NotBlank String username,

        @Schema(description = "Current password")
        @NotBlank String oldPassword,

        @Schema(description = "Replacement password")
        @NotBlank String newPassword
) {


}
