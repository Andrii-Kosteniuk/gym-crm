package com.training.gym.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Trainee training")
public record TraineeTrainingResponse(

        @Schema(description = "Training name")
        String trainingName,

        @JsonFormat(pattern = "dd.MM.yyyy")
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
