package com.training.gym.controller;

import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.controller.TrainingTypeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingTypeControllerMockMvcTest {

    private final GymCrmFacade facade = mock(GymCrmFacade.class);

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(
                        new TrainingTypeController(facade))
                .setControllerAdvice(
                        new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void getTypesAuthenticatesAndReturns200() throws Exception {

        when(facade.getTrainingTypes()).thenReturn(java.util.List.of());

        mvc.perform(get("/api/v1/specializations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(facade).getTrainingTypes();
    }
}
