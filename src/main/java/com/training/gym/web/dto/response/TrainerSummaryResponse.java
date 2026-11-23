package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trainer summary including specialization")
public record TrainerSummaryResponse(

        @Schema(description = "Account username")
        String userName,

        @Schema(description = "First name")
        String firstName,

        @Schema(description = "Last name")
        String lastName,

        @Schema(description = "Training specialization")
        TrainingTypeWebResponse type
) {
}
