package com.training.gym.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Username and generated password returned at registration and login")
public record AuthenticationResponse(

        @Schema(description = "Account username")
        String username,

        @Schema(description = "Password")
        String password
) {
}
