package com.training.gym.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.controller.TrainingTypeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerMockMvcTest {
    @Mock
    GymCrmFacade facade;
    @Mock
    CredentialsParser parser;
    @Mock
    AuthenticationService authenticationService;

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
        mvc = setup(new TrainingTypeController(facade, parser, authenticationService));
    }

    @Test
    void getTypesAuthenticatesAndReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);
        when(facade.getTrainingTypes()).thenReturn(java.util.List.of());

        // Act & Assert
        mvc.perform(get("/api/v1/specializations")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        var order = inOrder(authenticationService, facade);
        order.verify(authenticationService).authenticate("james", "pass");
        order.verify(facade).getTrainingTypes();
    }

    @Test
    void getTypesWithoutAuthorizationHeaderReturns400() throws Exception {
        // Act & Assert: current exception handler treats missing header as 400
        mvc.perform(get("/api/v1/specializations"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }
}
