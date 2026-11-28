package com.training.gym.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Username, generated password and access token is returned at registration")
public record RegistrationResponse(

        @Schema(description = "Account username")
        String username,

        @Schema(description = "Password")
        String password,

        @Schema(description = "Access token")
        String accessToken
) {
}
