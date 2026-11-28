package com.training.gym.security;

import com.training.gym.config.security.CorsConfig;
import com.training.gym.config.security.SecurityFilterChainConfig;
import com.training.gym.config.security.handler.RestAccessDeniedHandler;
import com.training.gym.config.security.handler.RestAuthenticationEntryPoint;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.controller.*;
import com.training.gym.web.dto.request.AuthRequest;
import com.training.gym.web.dto.request.TraineeRegistrationRequest;
import com.training.gym.web.dto.request.TrainerRegistrationRequest;
import com.training.gym.web.dto.response.AuthResponse;
import com.training.gym.web.dto.response.RegistrationResponse;
import com.training.gym.web.mapper.TraineeWebMapper;
import com.training.gym.web.mapper.TrainerWebMapper;
import com.training.gym.web.mapper.TrainingWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.*;

@SpringJUnitConfig(ControllerSecurityTest.Config.class)
@WebAppConfiguration
class ControllerSecurityTest {

    private static final String USERNAME = "james.bond";
    private static final String TOKEN = "test-token";
    private static final String PASSWORD = "password123";

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({
            SecurityFilterChainConfig.class,
            CorsConfig.class,
            RestAuthenticationEntryPoint.class,
            RestAccessDeniedHandler.class,
            AuthController.class,
            TraineeController.class,
            TrainerController.class,
            TrainingController.class,
            TrainingTypeController.class
    })
    static class Config {

        @Bean
        GymCrmFacade facade() {
            return mock(GymCrmFacade.class);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        TraineeWebMapper traineeWebMapper() {
            return mock(TraineeWebMapper.class);
        }

        @Bean
        TrainerWebMapper trainerWebMapper() {
            return mock(TrainerWebMapper.class);
        }

        @Bean
        TrainingWebMapper trainingWebMapper() {
            return mock(TrainingWebMapper.class);
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private GymCrmFacade facade;

    @Autowired
    private JwtDecoder jwtDecoder;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {

        reset(facade, jwtDecoder);

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldAllowTraineeRegistrationWithoutAuthentication()
            throws Exception {

        // Arrange
        var response = new RegistrationResponse(
                USERNAME, PASSWORD, TOKEN
        );

        when(facade.registerTrainee(
                any(TraineeRegistrationRequest.class)))
                .thenReturn(response);

        var request = post("/api/v1/trainees/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "firstName": "James",
                            "lastName": "Bond"
                        }
                        """);

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.password").value(PASSWORD))
                .andExpect(jsonPath("$.accessToken").value(TOKEN));

        verify(facade).registerTrainee(
                any(TraineeRegistrationRequest.class));

        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void shouldAllowTrainerRegistrationWithoutAuthentication()
            throws Exception {

        // Arrange
        var response = new RegistrationResponse(
                USERNAME, PASSWORD, TOKEN
        );

        when(facade.registerTrainer(
                any(TrainerRegistrationRequest.class)))
                .thenReturn(response);

        var request = post("/api/v1/trainers/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "firstName": "James",
                            "lastName": "Bond",
                            "specializationId": 1
                        }
                        """);

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.accessToken").value(TOKEN));

        verify(facade).registerTrainer(
                any(TrainerRegistrationRequest.class));

        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void shouldAllowLoginWithoutAuthentication()
            throws Exception {

        // Arrange
        var authRequest = new AuthRequest(USERNAME, PASSWORD);

        when(facade.authenticate(authRequest))
                .thenReturn(new AuthResponse(TOKEN));

        var request = post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "james.bond",
                            "password": "password123"
                        }
                        """);

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk());

        verify(facade).authenticate(authRequest);
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void shouldAllowRequestWithMockJwt() throws Exception {

        // Arrange
        when(facade.getTrainingTypes()).thenReturn(List.of());

        var request = get("/api/v1/specializations")
                .with(jwt().jwt(builder ->
                        builder.subject(USERNAME)));

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(facade).getTrainingTypes();

        // Mock JWT bypasses the actual decoder.
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void shouldAuthenticateBearerToken() throws Exception {

        // Arrange
        Jwt token = createJwt();

        when(jwtDecoder.decode(TOKEN)).thenReturn(token);
        when(facade.getTrainingTypes()).thenReturn(List.of());

        var request = get("/api/v1/specializations")
                .header("Authorization", "Bearer " + TOKEN);

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk());

        verify(jwtDecoder).decode(TOKEN);
        verify(facade).getTrainingTypes();
    }

    @Test
    void shouldRejectInvalidBearerToken() throws Exception {

        // Arrange
        when(jwtDecoder.decode(TOKEN))
                .thenThrow(new BadJwtException("Invalid JWT"));

        var request = get("/api/v1/specializations")
                .header("Authorization", "Bearer " + TOKEN);

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(header().exists("WWW-Authenticate"));

        verify(jwtDecoder).decode(TOKEN);
        verifyNoInteractions(facade);
    }

    @Test
    void shouldRejectBasicAuthentication() throws Exception {

        // Arrange
        var request = get("/api/v1/specializations")
                .header("Authorization", "Basic dXNlcjpwYXNz");

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isUnauthorized());

        verifyNoInteractions(facade, jwtDecoder);
    }

    @Test
    void shouldLogoutAuthenticatedUser() throws Exception {

        // Arrange
        Jwt token = createJwt();

        var request = post("/api/v1/auth/logout")
                .with(jwt().jwt(token));

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(facade).logOut(token);
    }

    @Test
    void shouldNotCreateAuthenticatedSession() throws Exception {

        // Arrange
        when(facade.getTrainingTypes()).thenReturn(List.of());

        var request = get("/api/v1/specializations")
                .with(jwt());

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(mvcResult ->
                        org.junit.jupiter.api.Assertions.assertNull(
                                mvcResult.getRequest()
                                        .getSession(false)
                        ));

        verify(facade).getTrainingTypes();
    }

    @Test
    void shouldAllowConfiguredCorsOrigin() throws Exception {

        // Arrange
        var request = options("/api/v1/trainings")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers",
                        "Authorization, Content-Type");

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:3000"));

        verifyNoInteractions(facade, jwtDecoder);
    }

    @Test
    void shouldRejectUnknownCorsOrigin() throws Exception {

        // Arrange
        var request = options("/api/v1/trainings")
                .header("Origin", "https://example.invalid")
                .header("Access-Control-Request-Method", "POST");

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isForbidden());

        verifyNoInteractions(facade, jwtDecoder);
    }

    @Test
    void shouldRejectUnsupportedCorsMethod() throws Exception {

        // Arrange
        var request = options("/api/v1/trainings")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "TRACE");

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isForbidden());

        verifyNoInteractions(facade, jwtDecoder);
    }

    @Test
    void shouldRejectUnsupportedCorsHeader() throws Exception {

        // Arrange
        var request = options("/api/v1/trainings")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers",
                        "X-Unsupported-Header");

        // Act
        var result = mvc.perform(request);

        // Assert
        result.andExpect(status().isForbidden());

        verifyNoInteractions(facade, jwtDecoder);
    }

    private Jwt createJwt() {

        Instant now = Instant.now();

        return Jwt.withTokenValue(TOKEN)
                .header("alg", "HS256")
                .subject(USERNAME)
                .claim("jti", "test-token-id")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .build();
    }
}