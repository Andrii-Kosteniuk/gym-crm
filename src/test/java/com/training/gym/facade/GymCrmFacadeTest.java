package com.training.gym.facade;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.auth.RegistrationResponseDto;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.mapper.TrainerMapper;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.User;
import com.training.gym.service.*;
import com.training.gym.validation.TraineeDtoValidator;
import com.training.gym.validation.TrainerDtoValidator;
import com.training.gym.validation.TrainingDtoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeTest {
    @Mock
    TraineeService trainees;
    @Mock
    TrainerService trainers;
    @Mock
    TrainingService trainings;
    @Mock
    TraineeDtoValidator traineeValidator;
    @Mock
    TrainerDtoValidator trainerValidator;
    @Mock
    TrainingDtoValidator trainingValidator;
    @Mock
    TraineeMapper traineeMapper;
    @Mock
    TrainerMapper trainerMapper;
    @Mock
    TrainingMapper trainingMapper;
    @Mock
    ProfileAuthorizationService authorization;
    @InjectMocks
    GymCrmFacade facade;

    private Credentials jamesCredentials() {
        return new Credentials("james.bond", "secret");
    }

    private Trainee james() {
        return Trainee.builder().id(1L).user(User.builder().id(1L).firstName("James").lastName("Bond").userName("james.bond").build()).build();
    }

    private Trainer sara() {
        return Trainer.builder().id(2L).user(User.builder().id(2L).firstName("Sara").lastName("Connor").userName("sara.connor").build()).build();
    }

    @Test
    void shouldRegisterTraineeWithoutAuthentication() {
        // Arrange
        CreateTraineeDto dto = new CreateTraineeDto("James", "Bond", LocalDate.of(1990, 4, 13), null);
        Trainee james = james();
        when(traineeMapper.toTrainee(dto)).thenReturn(james);
        when(trainees.registerTrainee(james)).thenReturn(new RegistrationResult<>(james, "raw-password"));
        // Act
        RegistrationResponseDto result = facade.createTrainee(dto);
        // Assert
        assertEquals("james.bond", result.username());
        assertEquals("raw-password", result.password());
        verify(traineeValidator).validateCreate(dto);
        verifyNoInteractions(authorization);
    }

    @Test
    void shouldRegisterTrainerWithoutAuthentication() {
        // Arrange
        CreateTrainerDto dto = new CreateTrainerDto("Sara", "Connor", 1L);
        Trainer sara = sara();
        when(trainerMapper.toTrainer(dto)).thenReturn(sara);
        when(trainers.registerTrainer(sara)).thenReturn(new RegistrationResult<>(sara, "raw-password"));
        // Act
        RegistrationResponseDto result = facade.createTrainer(dto);
        // Assert
        assertEquals("sara.connor", result.username());
        assertEquals("raw-password", result.password());
        verifyNoInteractions(authorization);
    }

    @Test
    void shouldAuthorizeBeforeReadingTrainee() {
        // Arrange
        Credentials credentials = jamesCredentials();
        Trainee james = james();
        when(trainees.findByUserName("james.bond")).thenReturn(james);
        // Act
        facade.getTraineeByUserName("james.bond", credentials);
        // Assert
        var order = inOrder(authorization, trainees);
        order.verify(authorization).requireTraineeOwner(credentials, "james.bond");
        order.verify(trainees).findByUserName("james.bond");
    }

    @Test
    void shouldAuthorizeBeforeUpdatingTrainee() {
        // Arrange
        Credentials credentials = jamesCredentials();
        UpdateTraineeDto dto = new UpdateTraineeDto("James", "Bond", "james.bond", LocalDate.of(1990, 4, 13), null);
        // Act
        facade.updateTrainee(dto, credentials);
        // Assert
        verify(traineeValidator).validateUpdate(dto);
        verify(authorization).requireTraineeOwner(credentials, "james.bond");
        verify(traineeMapper).toUpdateTrainee(dto);
    }

    @Test
    void shouldAuthorizeBeforeUpdatingTrainer() {
        // Arrange
        Credentials credentials = new Credentials("sara.connor", "secret");
        UpdateTrainerDto dto = new UpdateTrainerDto("Sara", "Connor", "sara.connor", 1L);
        // Act
        facade.updateTrainer(dto, credentials);
        // Assert
        verify(trainerValidator).validateUpdate(dto);
        verify(authorization).requireTrainerOwner(credentials, "sara.connor");
        verify(trainerMapper).updateTrainer(dto);
    }

    @Test
    void shouldAuthorizeBeforeDeletingTrainee() {
        // Arrange
        Credentials credentials = jamesCredentials();
        // Act
        facade.removeTraineeByUsername("james.bond", credentials);
        // Assert
        var order = inOrder(authorization, trainees);
        order.verify(authorization).requireTraineeOwner(credentials, "james.bond");
        order.verify(trainees).removeTraineeByUsername("james.bond");
    }

    @Test
    void shouldAuthorizePasswordChange() {
        // Arrange
        Credentials credentials = jamesCredentials();
        ChangePasswordDto dto = new ChangePasswordDto("old", "new");
        // Act
        facade.changeTraineePassword("james.bond", dto, credentials);
        // Assert
        verify(authorization).requireTraineeOwner(credentials, "james.bond");
        verify(trainees).changePassword("james.bond", "old", "new");
    }

    @Test
    void shouldActivateTraineeOnlyAfterOwnerCheck() {
        // Arrange
        Credentials credentials = jamesCredentials();
        Trainee james = james();
        when(trainees.getTraineeById(1L)).thenReturn(james);
        // Act
        facade.activateTrainee(1L, credentials);
        // Assert
        verify(authorization).requireTraineeOwner(credentials, "james.bond");
        verify(trainees).activateTrainee(james);
    }

    @Test
    void shouldActivateTrainerOnlyAfterOwnerCheck() {
        // Arrange
        Credentials credentials = new Credentials("sara.connor", "secret");
        Trainer sara = sara();
        when(trainers.getTrainerById(2L)).thenReturn(sara);
        // Act
        facade.activateTrainer(2L, credentials);
        // Assert
        verify(authorization).requireTrainerOwner(credentials, "sara.connor");
        verify(trainers).activateTrainer(sara);
    }

    @Test
    void shouldDelegateTrainingCreation() {
        // Arrange
        CreateTrainingDto dto = new CreateTrainingDto(1L, 2L, "Fitness", 1L, LocalDate.of(2026, 11, 15), 45L);
        Training training = Training.builder().build();
        when(trainingMapper.toTraining(dto)).thenReturn(training);
        // Act
        facade.createTraining(dto);
        // Assert
        verify(trainingValidator).validateCreate(dto);
        verify(trainings).createTraining(training);
    }

    @Test
    void shouldRejectNullTrainingId() {
        // Arrange
        Long id = null;
        // Act / Assert
        assertThrows(NullPointerException.class, () -> facade.getTrainingById(id));
        verifyNoInteractions(trainings);
    }

    @Test
    void shouldRejectNullTraineeUsername() {
        // Arrange
        String username = null;
        // Act / Assert
        assertThrows(NullPointerException.class, () -> facade.getTraineeByUserName(username, jamesCredentials()));
        verifyNoInteractions(authorization);
    }
}
