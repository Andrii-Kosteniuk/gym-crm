package com.training.gym.domain.criteria;

import java.time.LocalDate;

public record TraineeTrainingFilter(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        String trainingType
) {

    public TraineeTrainingFilter {
        validateDateRange(fromDate, toDate);

        trainerName = normalize(trainerName);
        trainingType = normalize(trainingType);
    }

    private static void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null
                && from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}