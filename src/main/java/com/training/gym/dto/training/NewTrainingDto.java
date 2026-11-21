package com.training.gym.dto.training;

import java.time.LocalDate;

public record NewTrainingDto(

        String traineeUsername,
        String trainerUsername,
        String trainingName,
        LocalDate date,
        Long duration
) {
}
