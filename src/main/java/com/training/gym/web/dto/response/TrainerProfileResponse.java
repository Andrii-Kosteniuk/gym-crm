package com.training.gym.web.dto.response;

import java.util.List;

public record TrainerProfileResponse(

        String userName,
        String firstName,
        String lastName,
        TrainingTypeWebResponse type,
        boolean active,
        List<TraineeSummaryResponse> trainees
) {
}
