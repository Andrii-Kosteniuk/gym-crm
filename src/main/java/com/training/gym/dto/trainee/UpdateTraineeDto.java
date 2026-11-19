package com.training.gym.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTraineeDto(

        @NotNull
        @NotBlank(message = "Name can not be empty")
        String firstName,

        @NotNull
        @NotBlank(message = "Last name can not be empty")
        String lastName,

        @NotNull
        @NotBlank(message = "Username can not be empty")
        String userName,

        // TODO I will create special annotation
        LocalDate dateOfBirth,

        // TODO I will create special annotation
        AddressDto address
) {
}
