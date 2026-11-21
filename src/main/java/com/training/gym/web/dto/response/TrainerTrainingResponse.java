package com.training.gym.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record TrainerTrainingResponse(

        String trainingName,
        @JsonFormat(pattern = "dd.MM.yyyy")
        LocalDate trainingDate,
        TrainingTypeWebResponse trainingType,
        long duration,
        String traineeName
) {
}
