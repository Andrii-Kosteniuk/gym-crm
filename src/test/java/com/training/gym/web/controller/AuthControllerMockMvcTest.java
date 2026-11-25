package com.training.gym.web.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerMockMvcTest {
    @Mock
    GymCrmFacade facade;
    @Mock
    CredentialsParser parser;
    @Mock
    AuthenticationService authenticationService;
    @Mock
    HttpHeaders headers;

    private MockMvc mvc;
    private static final String AUTH = "Basic am9objpwYXNz";
    private final Credentials credentials = new Credentials("james", "pass");

    private MockMvc setup(Object controller) {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @BeforeEach
    void setUp() {
        mvc = setup(new AuthController(facade, parser, authenticationService));
    }

    @Test
    void changePasswordReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(put("/api/v1/auth/login-change")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james","oldPassword":"pass","newPassword":"newPass"}
                                """))
                .andExpect(status().isOk());

        verify(authenticationService).authenticate("james", "pass");
        verify(facade).changePassword(any());
    }

    @Test
    void changePasswordRejectsMissingNewPassword() throws Exception {
        // Arrange

        // Act & Assert
        mvc.perform(put("/api/v1/auth/login-change")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james","oldPassword":"pass"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void shouldLoginReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(get("/api/v1/auth/login")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james","password":"pass"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectLoginWhenBadCredentials() throws Exception {
        // Arrange
        doThrow(AuthenticationFailedException.class)
                .when(parser).parse(AUTH);

        // Act & Assert
        mvc.perform(get("/api/v1/auth/login")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james","password":"wrongPassword"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(parser).parse(AUTH);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldRejectIfNoCredentialsWereProvided() throws Exception {

        String authorization = "Basic " + null;

        // Arrange
        doThrow(AuthenticationFailedException.class)
                .when(parser).parse(authorization);

        // Act & Assert
        mvc.perform(get("/api/v1/auth/login")
                        .header("Authorization", authorization))

                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authenticationService);
    }
}
