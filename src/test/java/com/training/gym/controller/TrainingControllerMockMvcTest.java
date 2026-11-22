package com.training.gym.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.training.NewTrainingDto;
import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.controller.TrainingController;
import com.training.gym.web.mapper.TrainingWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerMockMvcTest {
    @Mock
    GymCrmFacade facade;
    @Mock
    CredentialsParser parser;
    @Mock
    TrainingWebMapper trainingMapper;

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
        mvc = setup(new TrainingController(facade, parser, trainingMapper));
    }

    @Test
    void createTrainingReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);
        when(trainingMapper.toNewTrainingDto(any()))
                .thenReturn(mock(NewTrainingDto.class));

        // Act & Assert
        mvc.perform(post("/api/v1/trainings")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"traineeUsername":"james","trainerUsername":"mike",
                                 "trainingName":"Yoga","date":"2026-11-20","duration":60}
                                """))
                .andExpect(status().isOk());
        verify(facade).createTraining(any(com.training.gym.dto.training.NewTrainingDto.class), eq(credentials));
    }

    @Test
    void createTrainingRejectsNegativeDuration() throws Exception {
        // Act & Assert
        mvc.perform(post("/api/v1/trainings")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"traineeUsername":"james","trainerUsername":"mike",
                                 "trainingName":"Yoga","date":"2026-11-20","duration":-1}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }
}
