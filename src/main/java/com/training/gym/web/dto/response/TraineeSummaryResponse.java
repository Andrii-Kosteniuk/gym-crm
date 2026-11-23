package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trainee summary")
public record TraineeSummaryResponse(

        @Schema(description = "Account username")
        String userName,

        @Schema(description = "First name")
        String firstName,

        @Schema(description = "Last name")
        String lastName
) {
}
