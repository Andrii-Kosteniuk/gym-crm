package com.training.gym.dto.training;

import com.training.gym.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public record CreateTrainingDto(

        Long traineeId,
        Long trainerId,
        String name,
        TrainingType type,
        LocalDate date,
        Duration duration
) {
}
