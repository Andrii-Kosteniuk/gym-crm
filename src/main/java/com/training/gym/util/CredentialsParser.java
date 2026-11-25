package com.training.gym.util;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CredentialsParser {

    public Credentials parse(String authorization) {
        if (authorization == null
                || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            throw new AuthenticationFailedException();
        }

        try {
            byte[] decoded = Base64.getDecoder()
                    .decode(authorization.substring(6).trim());

            String value = new String(decoded, StandardCharsets.UTF_8);
            int separator = value.indexOf(':');

            if (separator <= 0 || separator == value.length() - 1) {
                throw new AuthenticationFailedException();
            }

            return new Credentials(
                    value.substring(0, separator),
                    value.substring(separator + 1)
            );
        } catch (IllegalArgumentException exception) {
            throw new AuthenticationFailedException();
        }
    }
}
