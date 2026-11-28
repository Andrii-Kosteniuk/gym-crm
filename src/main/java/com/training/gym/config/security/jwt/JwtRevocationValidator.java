package com.training.gym.config.security.jwt;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRevocationValidator implements OAuth2TokenValidator<Jwt> {

    private final JwtRevocationService revocationService;

    @Override
    public @NotNull OAuth2TokenValidatorResult validate(Jwt jwt) {

        String tokenId = jwt.getId();

        if (tokenId == null
                || tokenId.isBlank()
                || jwt.getExpiresAt() == null
                || revocationService.isRevoked(tokenId)) {

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "Token is invalid or revoked",
                            null
                    )
            );
        }

        return OAuth2TokenValidatorResult.success();
    }
}