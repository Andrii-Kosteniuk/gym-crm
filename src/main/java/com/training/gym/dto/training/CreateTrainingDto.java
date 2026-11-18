package com.training.gym.dto.training;

import java.time.LocalDate;

public record CreateTrainingDto(

        Long traineeId,
        Long trainerId,
        String name,
        Long trainingTypeId,
        LocalDate date,
        Long duration
) {
}
