package com.training.gym.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Schema(description = "Replacement list of trainer assignments")
public record UpdateTraineeTrainersRequest(

        @NotNull
        @Schema(description = "Complete replacement set of trainer usernames")
        Set<@NotBlank String> trainerUsernames
) {
}
