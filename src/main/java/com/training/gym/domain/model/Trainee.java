package com.training.gym.domain.model;

import com.training.gym.exception.UserAlreadyExistsException;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class Trainee {

    private Long id;

    private User user;

    private LocalDate dateOfBirth;

    private Address address;

    @Builder.Default
    private Set<Trainer> trainers = new HashSet<>();

    @Builder.Default
    private Set<Training> trainings = new HashSet<>();

    public void assignTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer);

        if (!trainers.add(trainer)) {
            throw new UserAlreadyExistsException("Trainer is already assigned");
        }
    }


    public void removeTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer);

        if (trainers.remove(trainer)) {
            trainer.removeTrainee(this);
        }
    }
}
