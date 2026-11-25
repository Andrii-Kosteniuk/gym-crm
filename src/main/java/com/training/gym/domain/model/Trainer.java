package com.training.gym.domain.model;

import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class Trainer {

    private Long id;

    private User user;

    private TrainingType specialization;

    @Builder.Default
    private Set<Trainee> trainees = new HashSet<>();

    @Builder.Default
    private Set<Training> trainings = new HashSet<>();


    public void removeTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee);

        if (trainees.remove(trainee)) {
            trainee.removeTrainer(this);
        }
    }
}
