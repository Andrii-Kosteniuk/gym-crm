package com.training.gym.domain.criteria;

import java.time.LocalDate;

public record TrainerTrainingFilter(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {

    public TrainerTrainingFilter {
        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }

        traineeName = traineeName == null
                || traineeName.isBlank()
                ? null
                : traineeName.trim();
    }
}