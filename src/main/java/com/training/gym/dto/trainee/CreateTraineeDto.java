package com.training.gym.dto.trainee;

import java.time.LocalDate;

public record CreateTraineeDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        AddressDto address
) {
}
