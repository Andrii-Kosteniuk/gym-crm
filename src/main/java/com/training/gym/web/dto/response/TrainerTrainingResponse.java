package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Trainer training")
public record TrainerTrainingResponse(

        @Schema(description = "Training session name")
        String trainingName,

        @Schema(description = "Training date")
        LocalDate trainingDate,

        @Schema(description = "Training specialization")
        TrainingTypeWebResponse trainingType,

        @Schema(description = "Duration in minutes")
        long duration,

        @Schema(description = "Trainee name")
        String traineeName
) {
}
