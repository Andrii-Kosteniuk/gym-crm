package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Editable trainer profile information; specialization is read-only")
public record UpdateTrainerRequest(

        @NotNull
        @NotBlank(message = "Username can not be empty")
        @Schema(description = "Account username; cannot be changed")
        String userName,

        @NotNull
        @NotBlank(message = "First name cannot be empty")
        @Schema(description = "First name")
        String firstName,

        @NotNull @NotBlank(message = "Last name cannot be empty")
        @Schema(description = "Last name")
        String lastName,

        @NotNull
        @Schema(description = "Activation status")
        Boolean active
) {
}
