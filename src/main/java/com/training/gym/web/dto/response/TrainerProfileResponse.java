package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Trainer profile including specialization")
public record TrainerProfileResponse(

        @Schema(description = "Account username")
        String userName,

        @Schema(description = "First name")
        String firstName,

        @Schema(description = "Last name")
        String lastName,

        @Schema(description = "Training specialization")
        TrainingTypeWebResponse type,

        @Schema(description = "Activation status")
        boolean active,

        @Schema(description = "Assigned trainees")
        List<TraineeSummaryResponse> trainees
) {
}
