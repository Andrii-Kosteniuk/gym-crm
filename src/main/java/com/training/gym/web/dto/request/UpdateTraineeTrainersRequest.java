package com.training.gym.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateTraineeTrainersRequest(

        @NotNull
        Set<@NotBlank String> trainerUsernames
) {
}
