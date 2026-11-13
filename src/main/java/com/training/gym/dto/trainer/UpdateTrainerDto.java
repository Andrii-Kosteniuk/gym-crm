package com.training.gym.dto.trainer;

import com.training.gym.model.TrainingType;


public record UpdateTrainerDto(
        String firstName,
        String lastName,
        String userName,
        TrainingType specialization
) {
}
