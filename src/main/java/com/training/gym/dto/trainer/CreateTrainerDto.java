package com.training.gym.dto.trainer;

public record CreateTrainerDto(

        String firstName,
        String lastName,
        Long specializationId
) {}
