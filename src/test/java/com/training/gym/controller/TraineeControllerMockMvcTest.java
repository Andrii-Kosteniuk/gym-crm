package com.training.gym.controller;

import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.web.controller.TraineeController;
import com.training.gym.web.dto.response.RegistrationResponse;
import com.training.gym.web.mapper.TraineeWebMapper;
import com.training.gym.web.mapper.TrainerWebMapper;
import com.training.gym.web.mapper.TrainingWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraineeControllerMockMvcTest {
    private final GymCrmFacade facade = mock(GymCrmFacade.class);
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(
                        new TraineeController(
                                facade,
                                mock(TraineeWebMapper.class),
                                mock(TrainerWebMapper.class),
                                mock(TrainingWebMapper.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator).build();
    }

    @Test
    void registerReturns200() throws Exception {

        when(facade.registerTrainee(any())).thenReturn(new RegistrationResponse("james.bond", "password", "token"));

        mvc.perform(post("/api/v1/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"James\", \"lastName\": \"Bond\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));

    }

    @Test
    void registerRejectsBlankFirstName() throws Exception {

        mvc.perform(post("/api/v1/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \" \", \"lastName\": \"Bond\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void getProfileReturns200() throws Exception {

        mvc.perform(get("/api/v1/trainees/james/profile"))
                .andExpect(status().isOk());

        verify(facade).getTraineeByUserName("james");
    }

    @Test
    void traineeStatusEndpointMustBeMapped() throws Exception {

        mvc.perform(patch("/api/v1/trainees/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"james\", \"active\": false}"))
                .andExpect(status().isOk());

        verify(facade).changeTraineeProfileStatus("james", false);
    }

    @Test
    void deleteProfileReturns200() throws Exception {

        mvc.perform(delete("/api/v1/trainees/james"))
                .andExpect(status().isOk());

        verify(facade).removeTraineeByUsername("james");
    }
}
