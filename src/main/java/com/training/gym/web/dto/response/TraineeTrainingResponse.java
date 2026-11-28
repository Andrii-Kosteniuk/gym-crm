package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Trainee training")
public record TraineeTrainingResponse(

        @Schema(description = "Training name")
        String trainingName,

        @Schema(description = "Training date")
        LocalDate trainingDate,

        @Schema(description = "Training specialization")
        TrainingTypeWebResponse type,

        @Schema(description = "Duration in minutes")
        long duration,

        @Schema(description = "Trainer name")
        String trainerName
) {
}
