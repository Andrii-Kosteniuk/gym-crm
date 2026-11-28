package com.training.gym.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank
        String username,

        @NotBlank
        String password

) {
}
