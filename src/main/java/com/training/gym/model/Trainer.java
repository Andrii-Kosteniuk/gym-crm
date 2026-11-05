package com.training.gym.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class Trainer extends User {

    private TrainingType specialization;

    @Override
    public String toString() {
        return """
                Trainer {
                %s,
                specialization: %s
                """.formatted(
                super.toString().indent(4).stripTrailing(),
                specialization.getName()
        );
    }
}
