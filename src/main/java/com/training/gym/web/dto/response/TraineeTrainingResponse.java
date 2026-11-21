package com.training.gym.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record TraineeTrainingResponse(

        String trainingName,
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate trainingDate,
        TrainingTypeWebResponse type,
        long duration,
        String trainerName
) {
}
