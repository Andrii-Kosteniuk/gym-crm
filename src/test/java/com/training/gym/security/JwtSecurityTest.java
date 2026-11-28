package com.training.gym.security;

import com.training.gym.config.security.jwt.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtSecurityTest {
    private final Clock clock = Clock.systemUTC();
    private final JwtConfig config = new JwtConfig();
    private final JwtProperties properties = new JwtProperties(Base64.getEncoder()
            .encodeToString(new byte[32]), "gym-test", Duration.ofMinutes(15));
    private final JwtRevocationService revocation = new JwtRevocationService(clock);
    private final JwtDecoder decoder = config.jwtDecoder(config.jwtSecretKey(properties), properties, new JwtRevocationValidator(revocation));
    private final JwtEncoder encoder = config.jwtEncoder(config.jwtSecretKey(properties));

    @Test
    void shouldAcceptValidToken() {
        // Arrange
        var service = new JwtTokenService(encoder, properties, clock);
        // Act
        Jwt jwt = decoder.decode(service.generateToken("user"));
        // Assert
        assertEquals("user", jwt.getSubject());
        assertNotNull(jwt.getId());
        assertEquals(Duration.ofMinutes(15), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertFalse(jwt.hasClaim("secret"));
    }

    @Test
    void shouldRejectRevokedToken() {
        // Arrange
        String token = new JwtTokenService(encoder, properties, clock).generateToken("user");
        Jwt jwt = decoder.decode(token);
        // Act
        revocation.revoke(jwt.getId(), jwt.getExpiresAt());
        // Assert
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        // Arrange
        var past = Clock.fixed(clock.instant().minusSeconds(3600), ZoneOffset.UTC);
        String token = new JwtTokenService(encoder, properties, past).generateToken("user");
        // Act & Assert
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void shouldRejectWrongIssuer() {
        // Arrange
        var other = new JwtProperties(properties.secret(), "other", properties.accessTokenTtl());
        String token = new JwtTokenService(encoder, other, clock).generateToken("user");
        // Act & Assert
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void shouldRejectWrongSignature() {
        // Arrange
        byte[] key = new byte[32];
        key[0] = 1;
        var other = new JwtProperties(Base64.getEncoder().encodeToString(key), properties.issuer(), properties.accessTokenTtl());
        String token = new JwtTokenService(config.jwtEncoder(config.jwtSecretKey(other)), other, clock)
                .generateToken("user");
        // Act & Assert
        assertThrows(JwtException.class, () -> decoder.decode(token));
    }
}
