package com.training.gym.controller;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.exception.GlobalExceptionHandler;
import com.training.gym.facade.GymCrmFacade;
import com.training.gym.util.CredentialsParser;
import com.training.gym.web.controller.TraineeController;
import com.training.gym.web.mapper.TraineeWebMapper;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerMockMvcTest {
    @Mock
    GymCrmFacade facade;
    @Mock
    CredentialsParser parser;
    @Mock
    TraineeWebMapper traineeMapper;
    @Mock
    TrainingWebMapper trainingMapper;
    @Mock
    TrainerWebMapper trainerWebMapper;


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
        mvc = setup(new TraineeController(facade, parser, traineeMapper, trainerWebMapper, trainingMapper));
    }

    @Test
    void registerReturns200() throws Exception {
        // Arrange: valid request
        when(traineeMapper.toCreateDto(any()))
                .thenReturn(mock(CreateTraineeDto.class));

        // Act & Assert
        mvc.perform(post("/api/v1/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"James","lastName":"Bond"}
                                """))
                .andExpect(status().isOk());
        verify(facade).createTrainee(any());
    }

    @Test
    void registerRejectsBlankFirstName() throws Exception {
        // Arrange: invalid first name

        // Act & Assert
        mvc.perform(post("/api/v1/trainees/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Bond"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }


    @Test
    void getProfileReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(get("/api/v1/trainees/james/profile")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk());
        verify(facade).getTraineeByUserName("james", credentials);
    }

    @Test
    void updateProfileReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);
        when(traineeMapper.toUpdateTraineeDto(any()))
                .thenReturn(mock(UpdateTraineeDto.class));

        // Act & Assert
        mvc.perform(put("/api/v1/trainees")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"james","firstName":"James","lastName":"Bond","active":true}
                                """))
                .andExpect(status().isOk());
        verify(facade).updateTrainee(any(), eq(credentials));
    }

    @Test
    void deleteProfileReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(delete("/api/v1/trainees/james")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk());
        verify(facade).removeTraineeByUsername("james", credentials);
    }

    @Test
    void unassignedTrainersReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);
        when(facade.getUnassignedTrainers("james", credentials))
                .thenReturn(java.util.List.of());

        // Act & Assert
        mvc.perform(get("/api/v1/trainees/james/unassigned-trainers")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(facade).getUnassignedTrainers("james", credentials);
    }

    @Test
    void replaceTrainersUsesUsernames() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);
        when(facade.replaceTraineeTrainersByUsername(
                eq("james"), anySet(), eq(credentials)))
                .thenReturn(java.util.List.of());

        // Act & Assert
        mvc.perform(put("/api/v1/trainees/james/trainers/updates")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trainerUsernames":["trainer.one","trainer.two"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(facade).replaceTraineeTrainersByUsername(
                eq("james"),
                eq(java.util.Set.of("trainer.one", "trainer.two")),
                eq(credentials));
    }

    @Test
    void replaceTrainersRejectsMissingList() throws Exception {
        // Act & Assert
        mvc.perform(put("/api/v1/trainees/james/trainers/updates")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }

    @Test
    void traineeTrainingsReturns200() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(get("/api/v1/trainees/trainings")
                        .header("Authorization", AUTH)
                        .param("username", "james")
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-12-31")
                        .param("trainerName", "Mike")
                        .param("trainingType", "Yoga"))
                .andExpect(status().isOk());
        verify(facade).getTraineeTrainings(eq("james"), any(), eq(credentials));
    }

    @Test
    void traineeTrainingsRejectsInvalidDate() throws Exception {
        // Act & Assert
        mvc.perform(get("/api/v1/trainees/trainings")
                        .header("Authorization", AUTH)
                        .param("username", "james")
                        .param("fromDate", "invalid"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(facade);
    }

    @Test
    void traineeStatusEndpointMustBeMapped() throws Exception {
        // Arrange
        when(parser.parse(AUTH)).thenReturn(credentials);

        // Act & Assert
        mvc.perform(patch("/api/v1/trainees/profile")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"james","active":false}
                                """))
                .andExpect(status().isOk());
        verify(facade).changeTraineeProfileStatus("james", false, credentials);
    }
}
