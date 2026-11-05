package com.training.gym.dto.trainee;

import com.training.gym.model.Address;

import java.time.LocalDate;

public record UpdateTraineeDto(
        String firstName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        Address address
) {
}
