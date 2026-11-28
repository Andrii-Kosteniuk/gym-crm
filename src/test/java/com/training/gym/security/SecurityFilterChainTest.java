package com.training.gym.security;

import com.training.gym.config.security.CorsConfig;
import com.training.gym.config.security.SecurityFilterChainConfig;
import com.training.gym.config.security.handler.RestAccessDeniedHandler;
import com.training.gym.config.security.handler.RestAuthenticationEntryPoint;
import com.training.gym.config.security.jwt.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(SecurityFilterChainTest.Config.class)
@WebAppConfiguration
class SecurityFilterChainTest {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityFilterChainConfig.class, CorsConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})

    static class Config {
        @Bean
        JwtRevocationService revocation() {
            return new JwtRevocationService(Clock.systemUTC());
        }

        @Bean
        JwtProperties properties() {
            return new JwtProperties(Base64.getEncoder().encodeToString(new byte[32]), "gym-test", Duration.ofMinutes(15));
        }

        @Bean
        JwtDecoder decoder(JwtProperties p, JwtRevocationService r) {
            var c = new JwtConfig();
            return c.jwtDecoder(c.jwtSecretKey(p), p, new JwtRevocationValidator(r));
        }

        @Bean
        JwtTokenService tokens(JwtProperties p) {
            var c = new JwtConfig();
            return new JwtTokenService(c.jwtEncoder(c.jwtSecretKey(p)), p, Clock.systemUTC());
        }

        @Bean
        Endpoints endpoints() {
            return new Endpoints();
        }
    }

    @RestController
    static class Endpoints {
        @GetMapping("/api/v1/specializations")
        String protectedEndpoint() {
            return "ok";
        }

        @PostMapping({"/api/v1/auth/login", "/api/v1/trainees/registration", "/api/v1/trainers/registration"})
        String publicEndpoint() {
            return "ok";
        }
    }

    @Autowired
    WebApplicationContext context;
    @Autowired
    JwtTokenService tokens;
    @Autowired
    JwtDecoder decoder;
    @Autowired
    JwtRevocationService revocation;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldRejectMissingToken() throws Exception {
        // Arrange
        var request = get("/api/v1/specializations");
        // Act
        var result = mvc.perform(request);
        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowValidToken() throws Exception {
        // Arrange
        var request = get("/api/v1/specializations")
                .header("Authorization", "Bearer " + tokens.generateToken("owner"));
        // Act
        var result = mvc.perform(request);
        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    void shouldRejectRevokedToken() throws Exception {
        // Arrange
        String token = tokens.generateToken("owner");
        var jwt = decoder.decode(token);
        revocation.revoke(jwt.getId(), jwt.getExpiresAt());
        // Act
        var result = mvc.perform(get("/api/v1/specializations")
                .header("Authorization", "Bearer " + token));
        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowPublicEndpoints() throws Exception {
        // Arrange
        String[] paths = {"/api/v1/auth/login", "/api/v1/trainees/registration", "/api/v1/trainers/registration"};
        for (String path : paths) {
            // Act
            var result = mvc.perform(post(path));
            // Assert
            result.andExpect(status().isOk());
        }
    }

    @Test
    void shouldAllowConfiguredCorsOrigin() throws Exception {
        // Arrange
        var request = options("/api/v1/specializations")
                .header("Origin", "http://localhost:3000").header("Access-Control-Request-Method", "GET").header("Access-Control-Request-Headers", "Authorization");
        // Act
        var result = mvc.perform(request);
        // Assert
        result.andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void shouldRejectUnknownCorsOrigin() throws Exception {
        // Arrange
        var request = options("/api/v1/specializations")
                .header("Origin", "https://example.invalid")
                .header("Access-Control-Request-Method", "GET");
        // Act
        var result = mvc.perform(request);
        // Assert
        result.andExpect(status().isForbidden());
    }
}
