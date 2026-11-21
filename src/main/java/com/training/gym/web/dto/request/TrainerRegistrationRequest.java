package com.training.gym.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TrainerRegistrationRequest(

        @NotNull @NotBlank(message = "First name cannot be empty")
        String firstName,

        @NotNull @NotBlank(message = "Last name cannot be empty")
        String lastName,

        @NotNull @Positive Long specializationId
) {
}
