package com.training.gym.dto.crireria;

import java.time.LocalDate;

public record TrainerTrainingCriteria(
        // TODO Add validation
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
