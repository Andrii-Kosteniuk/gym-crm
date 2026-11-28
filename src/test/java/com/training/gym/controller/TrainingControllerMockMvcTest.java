package com.training.gym.controller;

import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.controller.TrainingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingControllerMockMvcTest {
    private final GymCrmFacade facade = mock(GymCrmFacade.class);
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(
                new TrainingController(facade)).setControllerAdvice(
                new GlobalExceptionHandler()).setValidator(validator).build();
    }

    private String body(long duration) {
        return "{\"traineeUsername\":\"james\",\"trainerUsername\":\"mike\",\"trainingName\":\"Yoga\",\"date\":\"" +
                LocalDate.now().plusDays(2) + "\",\"duration\":" + duration + "}";
    }

    @Test
    void createTrainingReturns200() throws Exception {

        mvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(60)))
                .andExpect(status().isOk());

        verify(facade).createTraining(any());
    }

    @Test
    void createTrainingRejectsNegativeDuration() throws Exception {
        // Arrange
        mvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(-1)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }
}
