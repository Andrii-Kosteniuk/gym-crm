package com.training.gym.dto.trainee;

import java.time.LocalDate;

public record UpdateTraineeDto(

        String firstName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        AddressDto address
) {
}
