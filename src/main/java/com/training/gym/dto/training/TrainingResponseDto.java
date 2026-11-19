package com.training.gym.dto.training;

import java.time.LocalDate;

public record TrainingResponseDto(

        Long id,
        Long traineeId,
        Long trainerId,
        String trainingName,
        TrainingTypeResponseDto type,
        LocalDate trainingDate,
        Long duration
) {
}
