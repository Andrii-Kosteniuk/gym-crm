package com.training.gym.dto.crireria;

import java.time.LocalDate;

public record TraineeTrainingCriteria(
        // TODO Add validation
        String username,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        String trainingType
) {
}
