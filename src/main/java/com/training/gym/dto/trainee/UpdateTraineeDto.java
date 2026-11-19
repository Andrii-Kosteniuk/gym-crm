package com.training.gym.dto.trainee;

import java.time.LocalDate;

public record UpdateTraineeDto(

        Long id,
        String firstName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        AddressDto address
) {
}
