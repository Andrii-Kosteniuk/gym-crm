package com.training.gym.dto.training;

import java.time.Duration;
import java.time.LocalDate;

public record CreateTrainingDto(

        Long traineeId,
        Long trainerId,
        String name,
        TrainingTypeDto type,
        LocalDate date,
        Duration duration
) {
}
