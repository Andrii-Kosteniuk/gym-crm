package com.training.gym.dto.trainee;

import com.training.gym.dto.AddressResponse;
import com.training.gym.dto.trainer.TrainerResponseDto;

import java.time.LocalDate;
import java.util.List;

public record TraineeResponseDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        LocalDate dateOfBirth,
        AddressResponse address,
        boolean active,
        List<TrainerResponseDto> trainers
) {


}
