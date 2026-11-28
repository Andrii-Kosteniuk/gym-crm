package com.training.gym.web.dto.response;

import com.training.gym.dto.AddressResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Trainee profile details")
public record TraineeProfileResponse(

        @Schema(description = "Account username")
        String userName,

        @Schema(description = "First name")
        String firstName,

        @Schema(description = "Last name")
        String lastName,

        @Schema(description = "Date of birth")
        LocalDate dateOfBirth,

        @Schema(description = "Optional address")
        AddressResponse address,

        @Schema(description = "Activation status")
        boolean active,

        @Schema(description = "Assigned trainers")
        List<TrainerSummaryResponse> trainers
) {
}
