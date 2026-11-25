package com.training.gym.facade;

import com.training.gym.domain.mapper.TraineeMapper;
import com.training.gym.domain.mapper.TrainerMapper;
import com.training.gym.domain.mapper.TrainingMapper;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.User;
import com.training.gym.dto.auth.AuthenticationResponse;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.auth.RegistrationResult;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.CreateTrainerDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.service.auth.CredentialService;
import com.training.gym.service.auth.ProfileAuthorizationService;
import com.training.gym.service.trainee.TraineeService;
import com.training.gym.service.trainer.TrainerService;
import com.training.gym.service.training.TrainingService;
import com.training.gym.util.validation.TraineeDtoValidator;
import com.training.gym.util.validation.TrainerDtoValidator;
import com.training.gym.util.validation.TrainingDtoValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
    TraineeService traineeService;
    @Mock
    TrainerService trainerService;
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
    @Mock
    AuthenticationService authenticationService;
    @Mock
    CredentialService credentialService;
    @InjectMocks
    GymCrmFacade facade;

    private Credentials jamesCredentials() {
        return new Credentials("james.bond", "secret");
    }

    private Credentials sarahCredentials() {
        return new Credentials("sarah.connor", "secret");
    }

    private Trainee james() {
        return Trainee.builder().id(1L)
                .user(User.builder()
                        .id(1L)
                        .firstName("James")
                        .lastName("Bond")
                        .userName("james.bond")
                        .build()).build();
    }

    private Trainer sara() {
        return Trainer.builder().id(2L)
                .user(User.builder()
                        .id(2L)
                        .firstName("Sara")
                        .lastName("Connor")
                        .userName("sara.connor")
                        .build()).build();
    }

    @Test
    void shouldRegisterTraineeWithoutAuthentication() {
        // Arrange
        CreateTraineeDto dto = new CreateTraineeDto("James", "Bond", LocalDate.of(1990, 4, 13), null);
        Trainee james = james();
        when(traineeMapper.toTrainee(dto)).thenReturn(james);
        when(traineeService.registerTrainee(james)).thenReturn(new RegistrationResult<>(james, "raw-password"));
        doNothing().when(credentialService).verifyIfUserExists(dto.firstName(), dto.lastName());
        // Act
        AuthenticationResponse result = facade.createTrainee(dto);
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
        when(trainerService.registerTrainer(sara)).thenReturn(new RegistrationResult<>(sara, "raw-password"));
        // Act
        AuthenticationResponse result = facade.createTrainer(dto);
        // Assert
        assertEquals("sara.connor", result.username());
        assertEquals("raw-password", result.password());
        verifyNoInteractions(authorization);
    }

    @Test
    void shouldAuthorizeBeforeReadingTrainee() {
        // Arrange
        String username = "james.bond";
        Credentials credentials = jamesCredentials();
        Trainee james = james();

        when(traineeService.findByUserName(username)).thenReturn(james);

        // Act
        facade.getTraineeByUserName(username, credentials);

        // Assert
        InOrder order = inOrder(authorization, traineeService);

        order.verify(authorization).requireTraineeOwner(credentials, username);

        order.verify(traineeService).findByUserName(username);

        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldAuthorizeBeforeUpdatingTrainee() {
        // Arrange
        String username = "james.bond";
        Credentials credentials = jamesCredentials();

        UpdateTraineeDto dto = new UpdateTraineeDto(
                "James",
                "Bond",
                username,
                null, null
        );

        Trainee mappedTrainee = james();

        when(traineeMapper.toUpdateTrainee(dto)).thenReturn(mappedTrainee);

        when(traineeService.updateTrainee(mappedTrainee)).thenReturn(mappedTrainee);

        // Act
        facade.updateTrainee(dto, credentials);

        // Assert
        InOrder order = inOrder(authorization, traineeService);

        order.verify(authorization).requireTraineeOwner(credentials, username);

        order.verify(traineeService).updateTrainee(mappedTrainee);

        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldAuthorizeBeforeUpdatingTrainer() {
        // Arrange
        String username = "sarah.connor";
        Credentials credentials = sarahCredentials();

        UpdateTrainerDto dto = new UpdateTrainerDto(
                "Sara",
                "Connor",
                username
        );

        Trainer mappedTrainer = sara();

        when(trainerMapper.toUpdateTrainerDomain(dto)).thenReturn(mappedTrainer);

        when(trainerService.updateTrainer(mappedTrainer)).thenReturn(mappedTrainer);

        // Act
        facade.updateTrainer(dto, credentials);

        // Assert
        InOrder order = inOrder(authorization, trainerService);

        order.verify(authorization).requireTrainerOwner(credentials, username);

        order.verify(trainerService).updateTrainer(mappedTrainer);

        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldAuthorizeBeforeDeletingTrainee() {
        // Arrange
        String username = "james.bond";
        Credentials credentials = jamesCredentials();

        // Act
        facade.removeTraineeByUsername(username, credentials);

        // Assert
        InOrder order = inOrder(authorization, traineeService);

        order.verify(authorization).requireTraineeOwner(credentials, username);

        order.verify(traineeService).removeTraineeByUsername(username);

        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldAuthorizeBeforeChangingTraineePassword() {
        // Arrange
        String username = "james.bond";
        Credentials credentials = jamesCredentials();

        ChangePasswordDto dto = new ChangePasswordDto("old", "new");

        // Act
        facade.changeTraineePassword(username, dto, credentials);

        // Assert
        InOrder order = inOrder(authorization, traineeService);

        order.verify(authorization).requireTraineeOwner(credentials, username);

        order.verify(traineeService).changePassword(username, "old", "new");

        order.verifyNoMoreInteractions();
    }


    @Test
    void shouldChangeProfileStatusOnlyAfterOwnerCheck() {
        // Arrange
        String username = "james.bond";
        Credentials credentials = jamesCredentials();
        Trainee james = james();

        // Act
        facade.changeTraineeProfileStatus(username, true, credentials);

        // Assert
        InOrder order = inOrder(authorization, traineeService);

        order.verify(authorization).requireTraineeOwner(credentials, username);

        order.verify(traineeService).changeProfileStatus(james.getUser().getUserName(), true);

        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldActivateTrainerOnlyAfterOwnerCheck() {
        // Arrange
        String username = "sarah.connor";
        Credentials credentials = new Credentials("sara.connor", "secret");

        // Act
        facade.changeTrainerProfileStatus(username, true, credentials);

        // Assert

        verify(trainerService).changeProfileStatus(username, true);

        verifyNoMoreInteractions(trainerService);
    }


    @Test
    void shouldRejectNullTrainingId() {
        // Arrange
        Long id = null;
        // Act / Assert
        assertThrows(NullPointerException.class, () -> facade.getTrainingById(id, jamesCredentials()));
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
