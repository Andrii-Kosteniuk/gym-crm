package com.training.gym.dto.trainer;

import com.training.gym.model.Address;
import com.training.gym.model.TrainingType;

import java.time.LocalDate;

public record CreateTrainerDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Address address,
        TrainingType specialization
) {}
