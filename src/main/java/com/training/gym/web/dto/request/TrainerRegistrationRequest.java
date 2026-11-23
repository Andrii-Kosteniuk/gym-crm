package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "New trainer registration data; specialization is selected at registration")
public record TrainerRegistrationRequest(

        @NotNull @NotBlank(message = "First name cannot be empty")
        @Schema(description = "First name")
        String firstName,

        @NotNull @NotBlank(message = "Last name cannot be empty")
        @Schema(description = "Last name")
        String lastName,

        @Schema(description = "Training specialization ID selected during registration")
        @NotNull @Positive Long specializationId
) {
}
