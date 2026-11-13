package com.training.gym.dto.trainee;

import com.training.gym.model.Address;

import java.time.LocalDate;

public record TraineeResponseDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        Address address,
        boolean active
) {


}
