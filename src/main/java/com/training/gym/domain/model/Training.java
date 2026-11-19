package com.training.gym.domain.model;

import lombok.*;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Training {

    private Long id;
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private String trainingName;
    private LocalDate trainingDate;
    private Duration trainingDuration;

    public void setTrainingDate(LocalDate trainingDate) {
        Objects.requireNonNull(trainingDate, "Training date must not be null");

        if (trainingDate.isBefore(LocalDate.now(Clock.systemDefaultZone()))) {
            throw new IllegalArgumentException("trainingDate is in past");
        }

        this.trainingDate = trainingDate;
    }

    public void setTrainingDuration(Duration trainingDuration) {
        Objects.requireNonNull(trainingDuration, "Training duration must not be null");

        if (trainingDuration.isNegative() || trainingDuration.isZero()) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        this.trainingDuration = trainingDuration;
    }
}
