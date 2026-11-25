package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to activate or deactivate a profile")
public record ProfileStatusChangeRequest(

        @NotNull
        @NotBlank
        @Schema(description = "Account username")
        String username,

        @NotNull
        @Schema(description = "Current or requested activation status")
        Boolean active
) {
}
