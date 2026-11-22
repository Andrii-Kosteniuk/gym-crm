package com.training.gym.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UpdateTraineeRequest(

        @NotBlank
        String userName,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @Past
        LocalDate dateOfBirth,

        @Valid
        AddressRequest address,

        @NotNull
        Boolean active
) {
}
