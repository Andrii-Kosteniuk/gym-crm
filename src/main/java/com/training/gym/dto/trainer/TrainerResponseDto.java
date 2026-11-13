package com.training.gym.dto.trainer;

import com.training.gym.dto.training.TrainingTypeResponseDto;

public record TrainerResponseDto(

        Long id,
        String firstName,
        String lastName,
        String userName,
        boolean active,
        TrainingTypeResponseDto type
) {
}
