package com.training.gym.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileStatusChangeRequest(

        @NotNull
        @NotBlank
        String username,
        @NotNull
        Boolean active
) {
}
