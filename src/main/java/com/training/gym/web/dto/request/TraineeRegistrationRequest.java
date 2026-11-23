package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "New trainee registration data")
public record TraineeRegistrationRequest(

        @NotNull @NotBlank(message = "First name cannot be empty")
        @Schema(description = "First name")
        String firstName,

        @NotNull @NotBlank(message = "Last name cannot be empty")
        @Schema(description = "Last name")
        String lastName,

        @Past(message = "Date of birth must be the past date")
        @Schema(description = "Date of birth (optional)")
        LocalDate dateOfBirth,

        @Valid
        @Schema(description = "Address (optional)")
        AddressRequest address
) {
}
