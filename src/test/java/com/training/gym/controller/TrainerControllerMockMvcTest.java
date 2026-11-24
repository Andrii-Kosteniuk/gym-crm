package com.training.gym.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.controller.TrainerController;
import com.training.gym.web.mapper.TrainerWebMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerControllerMockMvcTest {
    @Mock
    GymCrmFacade facade;
    @Mock
    CredentialsParser parser;
    @Mock
    TrainerWebMapper trainerMapper;
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
        mvc = setup(new TrainerController(facade, parser, trainerMapper, trainingMapper));
    }

    @Test
    void registerReturns200() throws Exception {
        // Arrange
        when(trainerMapper.toCreateDto(any()))
                .thenReturn(mock(CreateTrainerDto.class));

        // Act & Assert
        mvc.perform(post("/api/v1/trainers/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Mike","lastName":"Jones","specializationId":1}
                                """))
                .andExpect(status().isOk());
        verify(facade).createTrainer(any());
    }

    @Test
    void registrationRejectsMissingSpecialization() throws Exception {
        // Act & Assert
        mvc.perform(post("/api/v1/trainers/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Mike","lastName":"Jones"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }


    @Test
    void getProfileReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(get("/api/v1/trainers/james/profile")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk());
        verify(facade).getTrainerByUserName("james", credentials);
    }

    @Test
    void updateProfileReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);
        when(trainerMapper.toUpdateDto(any()))
                .thenReturn(mock(UpdateTrainerDto.class));

        // Act & Assert
        mvc.perform(put("/api/v1/trainers")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"james","firstName":"Mike","lastName":"Jones",
                                 "specializationId":1,"active":true}
                                """))
                .andExpect(status().isOk());
        verify(facade).updateTrainer(any(), eq(credentials));
    }

    @Test
    void trainerTrainingsReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(get("/api/v1/trainers/trainings")
                        .header("Authorization", AUTH)
                        .param("username", "james")
                        .param("traineeName", "Bond"))
                .andExpect(status().isOk());
        verify(facade).getTrainerTrainings(eq("james"), any(), eq(credentials));
    }

    @Test
    void trainerStatusReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(patch("/api/v1/trainers/profile")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james","active":false}
                                """))
                .andExpect(status().isOk());
        verify(facade).changeTrainerProfileStatus("james", false, credentials);
    }

    @Test
    void trainerStatusRejectsMissingActive() throws Exception {
        // Act & Assert
        mvc.perform(patch("/api/v1/trainers/profile")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }
}
