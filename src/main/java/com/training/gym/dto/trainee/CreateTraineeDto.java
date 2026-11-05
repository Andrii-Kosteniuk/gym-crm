package com.training.gym.dto.trainee;

import com.training.gym.model.Address;

import java.time.LocalDate;

public record CreateTraineeDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Address address
) {
}
