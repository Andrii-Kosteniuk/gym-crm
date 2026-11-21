package com.training.gym.dto.trainee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CreateTraineeDto(
        @NotNull @NotBlank(message = "First name cannot be empty")
        String firstName,

        @NotNull @NotBlank(message = "Last name cannot be empty")
        String lastName,

        @Past(message = "Date of birth must be the past date")
        LocalDate dateOfBirth,

        @Valid
        AddressDto address
) {
}
