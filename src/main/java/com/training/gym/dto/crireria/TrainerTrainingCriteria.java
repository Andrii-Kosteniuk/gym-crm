package com.training.gym.dto.crireria;

import java.time.LocalDate;

public record TrainerTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
