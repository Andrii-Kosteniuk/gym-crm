package com.training.gym.web.dto.response;

public record TrainerSummaryResponse(

        String userName,
        String firstName,
        String lastName,
        TrainingTypeWebResponse type
) {
}
