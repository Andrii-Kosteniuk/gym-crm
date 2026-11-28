package com.training.gym.controller;

import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.controller.AuthController;
import com.training.gym.web.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerMockMvcTest {

    private final GymCrmFacade facade = mock(GymCrmFacade.class);

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvc = MockMvcBuilders.standaloneSetup(
                        new AuthController(facade))
                .setControllerAdvice(
                        new GlobalExceptionHandler())
                .setValidator(validator).build();
    }

    @Test
    void changePasswordReturns200() throws Exception {

        // Arrange Act
        mvc.perform(put("/api/v1/auth/login-change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"james\",\"oldPassword\":\"password\",\"newPassword\":\"newPass123\"}"))
                .andExpect(status().isOk());

        // Assert
        verify(facade).changePassword(any());
    }

    @Test
    void changePasswordRejectsMissingNewPassword() throws Exception {
        // Arrange Act
        mvc.perform(put("/api/v1/auth/login-change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"james\",\"oldPassword\":\"password\"}"))
                .andExpect(status().isBadRequest());
        // Assert

        verifyNoInteractions(facade);
    }

    @Test
    void loginReturns200() throws Exception {
        // Arrange Act
        when(facade.authenticate(any())).thenReturn(new AuthResponse("token"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"james\",\"password\":\"password\"}"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));

    }

    void logoutReturns204() throws Exception {

        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("james.bond")
                .claim("jti", "token-id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        var authentication = new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        doNothing().when(facade).logOut(jwt);

        // Act
        mvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());

        // Assert
        verify(facade).logOut(jwt);
    }
}
