package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTrainingRequest(

        @Schema(description = "Trainee username")
        @NotBlank String traineeUsername,

        @Schema(description = "Trainer username")
        @NotBlank String trainerUsername,

        @Schema(description = "Training name")
        @NotBlank String trainingName,

        @Schema(description = "Training date")
        @NotNull @FutureOrPresent LocalDate date,

        @Schema(description = "Duration in minutes")
        @NotNull @Positive Long duration
) {
}
