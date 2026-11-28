package com.training.gym.dto.training;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;

import java.time.LocalDate;

public record TrainingResponseDto(

        Long id,
        Trainee trainee,
        Trainer trainer,
        String trainingName,
        TrainingTypeResponseDto type,
        LocalDate trainingDate,
        Long duration
) {
}
