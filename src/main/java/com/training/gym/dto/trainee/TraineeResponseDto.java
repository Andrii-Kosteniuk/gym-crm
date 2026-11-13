package com.training.gym.dto.trainee;

import com.training.gym.dto.AddressResponse;

import java.time.LocalDate;

public record TraineeResponseDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        AddressResponse address,
        boolean active
) {


}
