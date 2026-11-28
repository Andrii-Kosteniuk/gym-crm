package com.training.gym.facade;

import com.training.gym.domain.mapper.TraineeMapper;
import com.training.gym.domain.mapper.TrainerMapper;
import com.training.gym.domain.mapper.TrainingMapper;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.dto.trainer.UpdateTrainerDto;
import com.training.gym.dto.training.TrainingResponseDto;
import com.training.gym.dto.training.TrainingTypeResponseDto;
import com.training.gym.exception.UserNotFoundException;
import com.training.gym.service.auth.AuthenticationService;
import com.training.gym.service.auth.CredentialService;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import com.training.gym.service.trainee.TraineeService;
import com.training.gym.service.trainer.TrainerService;
import com.training.gym.service.training.TrainingService;
import com.training.gym.service.types.TrainingTypeService;
import com.training.gym.util.validation.TraineeDtoValidator;
import com.training.gym.util.validation.TrainerDtoValidator;
import com.training.gym.web.dto.request.*;
import com.training.gym.web.dto.response.AuthResponse;
import com.training.gym.web.dto.response.RegistrationResponse;
import com.training.gym.web.mapper.TrainingWebMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeTest {

    private static final String TRAINEE_USERNAME = "james.bond";
    private static final String TRAINER_USERNAME = "Jane.Brown";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD = "newPassword";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TraineeDtoValidator traineeValidator;

    @Mock
    private TrainerDtoValidator trainerValidator;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private TrainingWebMapper trainingWebMapper;

    @Mock
    private TrainingTypePersistenceMapper trainingTypeMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private GymCrmFacade facade;

    @Test
    void shouldRegisterTraineeWithoutAuthentication() {

        // Arrange
        var request = mock(TraineeRegistrationRequest.class);
        var expected = mock(RegistrationResponse.class);

        when(authenticationService.registerTrainee(request))
                .thenReturn(expected);

        // Act
        var actual = facade.registerTrainee(request);

        // Assert
        assertSame(expected, actual);

        verify(authenticationService).registerTrainee(request);
        verifyNoInteractions(credentialService);
    }

    @Test
    void shouldRegisterTrainerWithoutAuthentication() {

        // Arrange
        var request = mock(TrainerRegistrationRequest.class);
        var expected = mock(RegistrationResponse.class);

        when(authenticationService.registerTrainer(request))
                .thenReturn(expected);

        // Act
        var actual = facade.registerTrainer(request);

        // Assert
        assertSame(expected, actual);

        verify(authenticationService).registerTrainer(request);
        verifyNoInteractions(credentialService);
    }

    @Test
    void shouldPropagateTraineeRegistrationException() {

        // Arrange
        var request = mock(TraineeRegistrationRequest.class);
        var exception = new IllegalArgumentException(
                "Invalid registration request"
        );

        when(authenticationService.registerTrainee(request))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                IllegalArgumentException.class,
                () -> facade.registerTrainee(request)
        );

        // Assert
        assertSame(exception, actual);

        verify(authenticationService).registerTrainee(request);
    }

    @Test
    void shouldPropagateTrainerRegistrationException() {

        // Arrange
        var request = mock(TrainerRegistrationRequest.class);
        var exception = new IllegalArgumentException(
                "Invalid registration request"
        );

        when(authenticationService.registerTrainer(request))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                IllegalArgumentException.class,
                () -> facade.registerTrainer(request)
        );

        // Assert
        assertSame(exception, actual);

        verify(authenticationService).registerTrainer(request);
    }

    @Test
    void shouldAuthenticateUser() {

        // Arrange
        var request = mock(AuthRequest.class);
        var expected = mock(AuthResponse.class);

        when(authenticationService.authenticate(request))
                .thenReturn(expected);

        // Act
        var actual = facade.authenticate(request);

        // Assert
        assertSame(expected, actual);

        verify(authenticationService).authenticate(request);
    }

    @Test
    void shouldRejectInvalidCredentials() {

        // Arrange
        var request = mock(AuthRequest.class);
        var exception = new BadCredentialsException("Invalid credentials");

        when(authenticationService.authenticate(request))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                BadCredentialsException.class,
                () -> facade.authenticate(request)
        );

        // Assert
        assertSame(exception, actual);

        verify(authenticationService).authenticate(request);
    }

    @Test
    void shouldLogoutUser() {

        // Arrange
        Jwt jwt = mock(Jwt.class);

        // Act
        facade.logOut(jwt);

        // Assert
        verify(authenticationService).logout(jwt);
    }

    @Test
    void shouldPropagateLogoutException() {

        // Arrange
        Jwt jwt = mock(Jwt.class);

        var exception = new IllegalArgumentException(
                "Invalid token"
        );

        doThrow(exception)
                .when(authenticationService)
                .logout(jwt);

        // Act
        var actual = assertThrows(
                IllegalArgumentException.class,
                () -> facade.logOut(jwt)
        );

        // Assert
        assertSame(exception, actual);

        verify(authenticationService).logout(jwt);
    }

    @Test
    void shouldValidateMapAndUpdateTrainee() {

        // Arrange
        var dto = mock(UpdateTraineeDto.class);
        var trainee = mock(Trainee.class);
        var updatedTrainee = mock(Trainee.class);
        var expected = mock(TraineeResponseDto.class);

        when(traineeMapper.toUpdateTrainee(dto))
                .thenReturn(trainee);

        when(traineeService.updateTrainee(trainee))
                .thenReturn(updatedTrainee);

        when(traineeMapper.toResponseDto(updatedTrainee))
                .thenReturn(expected);

        // Act
        var actual = facade.updateTrainee(dto);

        // Assert
        assertSame(expected, actual);

        InOrder inOrder = inOrder(
                traineeValidator,
                traineeMapper,
                traineeService
        );

        inOrder.verify(traineeValidator).validateUpdate(dto);
        inOrder.verify(traineeMapper).toUpdateTrainee(dto);
        inOrder.verify(traineeService).updateTrainee(trainee);
        inOrder.verify(traineeMapper).toResponseDto(updatedTrainee);
    }

    @Test
    void shouldNotUpdateTraineeWhenValidationFails() {

        // Arrange
        var dto = mock(UpdateTraineeDto.class);
        var exception = new IllegalArgumentException(
                "Invalid trainee update"
        );

        doThrow(exception)
                .when(traineeValidator)
                .validateUpdate(dto);

        // Act
        var actual = assertThrows(
                IllegalArgumentException.class,
                () -> facade.updateTrainee(dto)
        );

        // Assert
        assertSame(exception, actual);

        verify(traineeValidator).validateUpdate(dto);
        verifyNoInteractions(traineeMapper, traineeService);
    }

    @Test
    void shouldNotMapUpdatedTraineeWhenAuthorizationFails() {

        // Arrange
        var dto = mock(UpdateTraineeDto.class);
        var trainee = mock(Trainee.class);

        when(traineeMapper.toUpdateTrainee(dto))
                .thenReturn(trainee);

        when(traineeService.updateTrainee(trainee))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.updateTrainee(dto)
        );

        // Assert
        verify(traineeValidator).validateUpdate(dto);
        verify(traineeMapper).toUpdateTrainee(dto);
        verify(traineeService).updateTrainee(trainee);

        verify(traineeMapper, never())
                .toResponseDto(any(Trainee.class));
    }

    @Test
    void shouldReadAndMapTraineeByUsername() {

        // Arrange
        var trainee = mock(Trainee.class);
        var expected = mock(TraineeResponseDto.class);

        when(traineeService.findByUserName(TRAINEE_USERNAME))
                .thenReturn(trainee);

        when(traineeMapper.toResponseDto(trainee))
                .thenReturn(expected);

        // Act
        var actual = facade.getTraineeByUserName(TRAINEE_USERNAME);

        // Assert
        assertSame(expected, actual);

        InOrder inOrder = inOrder(traineeService, traineeMapper);

        inOrder.verify(traineeService)
                .findByUserName(TRAINEE_USERNAME);

        inOrder.verify(traineeMapper).toResponseDto(trainee);
    }

    @Test
    void shouldRejectNullTraineeUsername() {

        // Arrange
        String username = null;

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> facade.getTraineeByUserName(username)
        );

        // Assert
        assertEquals(
                GymCrmFacade.USERNAME_CAN_NOT_BE_NULL,
                exception.getMessage()
        );

        verifyNoInteractions(traineeService, traineeMapper);
    }

    @Test
    void shouldNotMapTraineeWhenLookupFails() {

        // Arrange
        var exception = new UserNotFoundException(
                "Trainee not found"
        );

        when(traineeService.findByUserName(TRAINEE_USERNAME))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                UserNotFoundException.class,
                () -> facade.getTraineeByUserName(TRAINEE_USERNAME)
        );

        // Assert
        assertSame(exception, actual);

        verify(traineeService).findByUserName(TRAINEE_USERNAME);
        verifyNoInteractions(traineeMapper);
    }

    @Test
    void shouldNotMapTraineeWhenAuthorizationFails() {

        // Arrange
        when(traineeService.findByUserName(TRAINEE_USERNAME))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.getTraineeByUserName(TRAINEE_USERNAME)
        );

        // Assert
        verify(traineeService).findByUserName(TRAINEE_USERNAME);
        verifyNoInteractions(traineeMapper);
    }

    @Test
    void shouldValidateMapAndUpdateTrainer() {

        // Arrange
        var dto = mock(UpdateTrainerDto.class);
        var trainer = mock(Trainer.class);
        var updatedTrainer = mock(Trainer.class);
        var expected = mock(TrainerResponseDto.class);

        when(trainerMapper.toUpdateTrainerDomain(dto))
                .thenReturn(trainer);

        when(trainerService.updateTrainer(trainer))
                .thenReturn(updatedTrainer);

        when(trainerMapper.toResponseDto(updatedTrainer))
                .thenReturn(expected);

        // Act
        var actual = facade.updateTrainer(dto);

        // Assert
        assertSame(expected, actual);

        InOrder inOrder = inOrder(
                trainerValidator,
                trainerMapper,
                trainerService
        );

        inOrder.verify(trainerValidator).validateUpdate(dto);
        inOrder.verify(trainerMapper).toUpdateTrainerDomain(dto);
        inOrder.verify(trainerService).updateTrainer(trainer);
        inOrder.verify(trainerMapper).toResponseDto(updatedTrainer);
    }

    @Test
    void shouldNotUpdateTrainerWhenValidationFails() {

        // Arrange
        var dto = mock(UpdateTrainerDto.class);
        var exception = new IllegalArgumentException(
                "Invalid trainer update"
        );

        doThrow(exception)
                .when(trainerValidator)
                .validateUpdate(dto);

        // Act
        var actual = assertThrows(
                IllegalArgumentException.class,
                () -> facade.updateTrainer(dto)
        );

        // Assert
        assertSame(exception, actual);

        verify(trainerValidator).validateUpdate(dto);
        verifyNoInteractions(trainerMapper, trainerService);
    }

    @Test
    void shouldNotMapUpdatedTrainerWhenAuthorizationFails() {

        // Arrange
        var dto = mock(UpdateTrainerDto.class);
        var trainer = mock(Trainer.class);

        when(trainerMapper.toUpdateTrainerDomain(dto))
                .thenReturn(trainer);

        when(trainerService.updateTrainer(trainer))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.updateTrainer(dto)
        );

        // Assert
        verify(trainerValidator).validateUpdate(dto);
        verify(trainerMapper).toUpdateTrainerDomain(dto);
        verify(trainerService).updateTrainer(trainer);

        verify(trainerMapper, never())
                .toResponseDto(any(Trainer.class));
    }

    @Test
    void shouldReadAndMapTrainerByUsername() {

        // Arrange
        var trainer = mock(Trainer.class);
        var expected = mock(TrainerResponseDto.class);

        when(trainerService.findByUserName(TRAINER_USERNAME))
                .thenReturn(trainer);

        when(trainerMapper.toResponseDto(trainer))
                .thenReturn(expected);

        // Act
        var actual = facade.getTrainerByUserName(TRAINER_USERNAME);

        // Assert
        assertSame(expected, actual);

        InOrder inOrder = inOrder(trainerService, trainerMapper);

        inOrder.verify(trainerService)
                .findByUserName(TRAINER_USERNAME);

        inOrder.verify(trainerMapper).toResponseDto(trainer);
    }

    @Test
    void shouldRejectNullUsernameBeforeCallingDependencies() {

        // Arrange
        String username = null;

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> facade.getTrainerByUserName(username)
        );

        // Assert
        assertEquals(
                GymCrmFacade.USERNAME_CAN_NOT_BE_NULL,
                exception.getMessage()
        );

        verifyNoInteractions(trainerService, trainerMapper);
    }

    @Test
    void shouldNotMapTrainerWhenUsernameLookupFails() {

        // Arrange
        var exception = new UserNotFoundException(
                "Trainer not found"
        );

        when(trainerService.findByUserName(TRAINER_USERNAME))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                UserNotFoundException.class,
                () -> facade.getTrainerByUserName(TRAINER_USERNAME)
        );

        // Assert
        assertSame(exception, actual);

        verify(trainerService).findByUserName(TRAINER_USERNAME);
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void shouldNotMapTrainerWhenAuthorizationFails() {

        // Arrange
        when(trainerService.findByUserName(TRAINER_USERNAME))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.getTrainerByUserName(TRAINER_USERNAME)
        );

        // Assert
        verify(trainerService).findByUserName(TRAINER_USERNAME);
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void shouldDelegatePasswordChange() {

        // Arrange
        var dto = mock(ChangeLoginRequestDto.class);

        when(dto.username()).thenReturn(TRAINEE_USERNAME);
        when(dto.oldPassword()).thenReturn(OLD_PASSWORD);
        when(dto.newPassword()).thenReturn(NEW_PASSWORD);

        // Act
        facade.changePassword(dto);

        // Assert
        verify(credentialService).changePassword(
                TRAINEE_USERNAME,
                OLD_PASSWORD,
                NEW_PASSWORD
        );

        verifyNoInteractions(
                authenticationService,
                traineeService,
                trainerService
        );
    }

    @Test
    void shouldRejectNullPasswordChangeDtoBeforeCallingDependencies() {

        // Arrange
        ChangeLoginRequestDto dto = null;

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> facade.changePassword(dto)
        );

        // Assert
        assertEquals(
                "Password change DTO is required",
                exception.getMessage()
        );

        verifyNoInteractions(credentialService);
    }

    @Test
    void shouldRejectPasswordChangeWhenOldPasswordIsIncorrect() {

        // Arrange
        var dto = mock(ChangeLoginRequestDto.class);

        when(dto.username()).thenReturn(TRAINEE_USERNAME);
        when(dto.oldPassword()).thenReturn(OLD_PASSWORD);
        when(dto.newPassword()).thenReturn(NEW_PASSWORD);

        var exception = new BadCredentialsException(
                "Invalid credentials"
        );

        doThrow(exception)
                .when(credentialService)
                .changePassword(
                        TRAINEE_USERNAME,
                        OLD_PASSWORD,
                        NEW_PASSWORD
                );

        // Act
        var actual = assertThrows(
                BadCredentialsException.class,
                () -> facade.changePassword(dto)
        );

        // Assert
        assertSame(exception, actual);

        verify(credentialService).changePassword(
                TRAINEE_USERNAME,
                OLD_PASSWORD,
                NEW_PASSWORD
        );
    }

    @Test
    void shouldRejectUnauthorizedPasswordChange() {

        // Arrange
        var dto = mock(ChangeLoginRequestDto.class);

        when(dto.username()).thenReturn(TRAINEE_USERNAME);
        when(dto.oldPassword()).thenReturn(OLD_PASSWORD);
        when(dto.newPassword()).thenReturn(NEW_PASSWORD);

        doThrow(new AccessDeniedException("Access denied"))
                .when(credentialService)
                .changePassword(
                        TRAINEE_USERNAME,
                        OLD_PASSWORD,
                        NEW_PASSWORD
                );

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.changePassword(dto)
        );

        // Assert
        verify(credentialService).changePassword(
                TRAINEE_USERNAME,
                OLD_PASSWORD,
                NEW_PASSWORD
        );
    }

    @Test
    void shouldActivateTrainee() {

        // Arrange
        boolean active = true;

        // Act
        facade.changeTraineeProfileStatus(
                TRAINEE_USERNAME,
                active
        );

        // Assert
        verify(traineeService).changeProfileStatus(
                TRAINEE_USERNAME,
                active
        );
    }

    @Test
    void shouldDeactivateTrainee() {

        // Arrange
        boolean active = false;

        // Act
        facade.changeTraineeProfileStatus(
                TRAINEE_USERNAME,
                active
        );

        // Assert
        verify(traineeService).changeProfileStatus(
                TRAINEE_USERNAME,
                active
        );
    }

    @Test
    void shouldNotDeactivateTraineeWhenAuthorizationFails() {

        // Arrange
        doThrow(new AccessDeniedException("Access denied"))
                .when(traineeService)
                .changeProfileStatus(TRAINEE_USERNAME, false);

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.changeTraineeProfileStatus(
                        TRAINEE_USERNAME, false
                )
        );

        // Assert
        verify(traineeService)
                .changeProfileStatus(TRAINEE_USERNAME, false);
    }

    @Test
    void shouldNotDeactivateMissingTrainee() {

        // Arrange
        var exception = new UserNotFoundException(
                "Trainee not found"
        );

        doThrow(exception)
                .when(traineeService)
                .changeProfileStatus(TRAINEE_USERNAME, false);

        // Act
        var actual = assertThrows(
                UserNotFoundException.class,
                () -> facade.changeTraineeProfileStatus(
                        TRAINEE_USERNAME, false
                )
        );

        // Assert
        assertSame(exception, actual);

        verify(traineeService)
                .changeProfileStatus(TRAINEE_USERNAME, false);
    }

    @Test
    void shouldActivateTrainer() {

        // Arrange
        boolean active = true;

        // Act
        facade.changeTrainerProfileStatus(
                TRAINER_USERNAME,
                active
        );

        // Assert
        verify(trainerService).changeProfileStatus(
                TRAINER_USERNAME,
                active
        );
    }

    @Test
    void shouldDeactivateTrainer() {

        // Arrange
        boolean active = false;

        // Act
        facade.changeTrainerProfileStatus(
                TRAINER_USERNAME,
                active
        );

        // Assert
        verify(trainerService).changeProfileStatus(
                TRAINER_USERNAME,
                active
        );
    }

    @Test
    void shouldNotDeactivateTrainerWhenAuthorizationFails() {

        // Arrange
        doThrow(new AccessDeniedException("Access denied"))
                .when(trainerService)
                .changeProfileStatus(TRAINER_USERNAME, false);

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.changeTrainerProfileStatus(
                        TRAINER_USERNAME, false
                )
        );

        // Assert
        verify(trainerService)
                .changeProfileStatus(TRAINER_USERNAME, false);
    }

    @Test
    void shouldNotDeactivateMissingTrainer() {

        // Arrange
        var exception = new UserNotFoundException(
                "Trainer not found"
        );

        doThrow(exception)
                .when(trainerService)
                .changeProfileStatus(TRAINER_USERNAME, false);

        // Act
        var actual = assertThrows(
                UserNotFoundException.class,
                () -> facade.changeTrainerProfileStatus(
                        TRAINER_USERNAME, false
                )
        );

        // Assert
        assertSame(exception, actual);

        verify(trainerService)
                .changeProfileStatus(TRAINER_USERNAME, false);
    }

    @Test
    void shouldDeleteTraineeByUsername() {

        // Arrange
        String username = TRAINEE_USERNAME;

        // Act
        facade.removeTraineeByUsername(username);

        // Assert
        verify(traineeService).removeTraineeByUsername(username);
    }

    @Test
    void shouldRejectNullUsernameWhenDeletingTrainee() {

        // Arrange
        String username = null;

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> facade.removeTraineeByUsername(username)
        );

        // Assert
        assertEquals(
                GymCrmFacade.USERNAME_CAN_NOT_BE_NULL,
                exception.getMessage()
        );

        verifyNoInteractions(traineeService);
    }

    @Test
    void shouldRejectUnauthorizedTraineeDeletion() {

        // Arrange
        doThrow(new AccessDeniedException("Access denied"))
                .when(traineeService)
                .removeTraineeByUsername(TRAINEE_USERNAME);

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.removeTraineeByUsername(TRAINEE_USERNAME)
        );

        // Assert
        verify(traineeService)
                .removeTraineeByUsername(TRAINEE_USERNAME);
    }

    @Test
    void shouldReplaceTraineeTrainersByUsername() {

        // Arrange
        Set<String> usernames = Set.of(
                TRAINER_USERNAME,
                "Alice.Williams"
        );

        var trainee = mock(Trainee.class);
        var firstTrainer = mock(Trainer.class);
        var secondTrainer = mock(Trainer.class);

        var firstResponse = mock(TrainerResponseDto.class);
        var secondResponse = mock(TrainerResponseDto.class);

        when(traineeService.findByUserName(TRAINEE_USERNAME))
                .thenReturn(trainee);

        when(trainee.getTrainers())
                .thenReturn(Set.of(firstTrainer, secondTrainer));

        when(trainerMapper.toResponseDto(firstTrainer))
                .thenReturn(firstResponse);

        when(trainerMapper.toResponseDto(secondTrainer))
                .thenReturn(secondResponse);

        // Act
        var actual = facade.replaceTraineeTrainersByUsername(
                TRAINEE_USERNAME,
                usernames
        );

        // Assert
        assertAll(
                () -> assertEquals(2, actual.size()),
                () -> assertTrue(actual.contains(firstResponse)),
                () -> assertTrue(actual.contains(secondResponse))
        );

        InOrder inOrder = inOrder(traineeService);

        inOrder.verify(traineeService)
                .replaceTrainersByUsername(
                        TRAINEE_USERNAME, usernames
                );

        inOrder.verify(traineeService)
                .findByUserName(TRAINEE_USERNAME);

        verify(trainerMapper).toResponseDto(firstTrainer);
        verify(trainerMapper).toResponseDto(secondTrainer);
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainersRemainAssigned() {

        // Arrange
        Set<String> usernames = Set.of();
        var trainee = mock(Trainee.class);

        when(traineeService.findByUserName(TRAINEE_USERNAME))
                .thenReturn(trainee);

        when(trainee.getTrainers()).thenReturn(Set.of());

        // Act
        var actual = facade.replaceTraineeTrainersByUsername(
                TRAINEE_USERNAME,
                usernames
        );

        // Assert
        assertNotNull(actual);
        assertTrue(actual.isEmpty());

        verify(traineeService).replaceTrainersByUsername(
                TRAINEE_USERNAME,
                usernames
        );

        verify(traineeService).findByUserName(TRAINEE_USERNAME);
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void shouldNotReadTraineeWhenTrainerReplacementFails() {

        // Arrange
        Set<String> usernames = Set.of(TRAINER_USERNAME);

        doThrow(new AccessDeniedException("Access denied"))
                .when(traineeService)
                .replaceTrainersByUsername(
                        TRAINEE_USERNAME, usernames
                );

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.replaceTraineeTrainersByUsername(
                        TRAINEE_USERNAME, usernames
                )
        );

        // Assert
        verify(traineeService).replaceTrainersByUsername(
                TRAINEE_USERNAME,
                usernames
        );

        verify(traineeService, never())
                .findByUserName(anyString());

        verifyNoInteractions(trainerMapper);
    }

    @Test
    void returnsMappedUnassignedTrainers() {

        // Arrange
        var firstTrainer = mock(Trainer.class);
        var secondTrainer = mock(Trainer.class);

        var firstResponse = mock(TrainerResponseDto.class);
        var secondResponse = mock(TrainerResponseDto.class);

        when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of(firstTrainer, secondTrainer));

        when(trainerMapper.toResponseDto(firstTrainer))
                .thenReturn(firstResponse);

        when(trainerMapper.toResponseDto(secondTrainer))
                .thenReturn(secondResponse);

        // Act
        var actual = facade.getUnassignedTrainers(TRAINEE_USERNAME);

        // Assert
        assertEquals(
                List.of(firstResponse, secondResponse),
                actual
        );

        verify(trainerService)
                .getUnassignedTrainers(TRAINEE_USERNAME);

        verify(trainerMapper).toResponseDto(firstTrainer);
        verify(trainerMapper).toResponseDto(secondTrainer);
    }

    @Test
    void returnsEmptyListWhenNoTrainersAvailable() {

        // Arrange
        when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of());

        // Act
        var actual = facade.getUnassignedTrainers(TRAINEE_USERNAME);

        // Assert
        assertNotNull(actual);
        assertTrue(actual.isEmpty());

        verify(trainerService)
                .getUnassignedTrainers(TRAINEE_USERNAME);

        verifyNoInteractions(trainerMapper);
    }

    @Test
    void rejectsNullUsername() {

        // Arrange
        String username = null;

        // Act
        var exception = assertThrows(
                NullPointerException.class,
                () -> facade.getUnassignedTrainers(username)
        );

        // Assert
        assertEquals(
                GymCrmFacade.USERNAME_CAN_NOT_BE_NULL,
                exception.getMessage()
        );

        verifyNoInteractions(trainerService, trainerMapper);
    }

    @Test
    void deniesUnauthorizedAccess() {

        // Arrange
        when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.getUnassignedTrainers(TRAINEE_USERNAME)
        );

        // Assert
        verify(trainerService)
                .getUnassignedTrainers(TRAINEE_USERNAME);

        verifyNoInteractions(trainerMapper);
    }

    @Test
    void returnsMappedTraineeTrainings() {

        // Arrange
        var criteria = mock(TraineeTrainingCriteria.class);

        var firstTraining = mock(Training.class);
        var secondTraining = mock(Training.class);

        var firstResponse = mock(TrainingResponseDto.class);
        var secondResponse = mock(TrainingResponseDto.class);

        when(traineeService.findTrainings(TRAINEE_USERNAME, criteria))
                .thenReturn(List.of(firstTraining, secondTraining));

        when(trainingMapper.toResponseDto(firstTraining))
                .thenReturn(firstResponse);

        when(trainingMapper.toResponseDto(secondTraining))
                .thenReturn(secondResponse);

        // Act
        var actual = facade.getTraineeTrainings(
                TRAINEE_USERNAME,
                criteria
        );

        // Assert
        assertEquals(
                List.of(firstResponse, secondResponse),
                actual
        );

        verify(traineeService).findTrainings(
                TRAINEE_USERNAME,
                criteria
        );

        verify(trainingMapper).toResponseDto(firstTraining);
        verify(trainingMapper).toResponseDto(secondTraining);
    }

    @Test
    void shouldReturnEmptyListWhenNoTraineeTrainingsMatch() {

        // Arrange
        var criteria = mock(TraineeTrainingCriteria.class);

        when(traineeService.findTrainings(TRAINEE_USERNAME, criteria))
                .thenReturn(List.of());

        // Act
        var actual = facade.getTraineeTrainings(
                TRAINEE_USERNAME,
                criteria
        );

        // Assert
        assertNotNull(actual);
        assertTrue(actual.isEmpty());

        verify(traineeService).findTrainings(
                TRAINEE_USERNAME,
                criteria
        );

        verifyNoInteractions(trainingMapper);
    }

    @Test
    void rejectsUnauthorizedTraineeSearch() {

        // Arrange
        var criteria = mock(TraineeTrainingCriteria.class);

        when(traineeService.findTrainings(TRAINEE_USERNAME, criteria))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.getTraineeTrainings(
                        TRAINEE_USERNAME, criteria
                )
        );

        // Assert
        verify(traineeService).findTrainings(
                TRAINEE_USERNAME,
                criteria
        );

        verifyNoInteractions(trainingMapper);
    }

    @Test
    void returnsMappedTrainerTrainings() {

        // Arrange
        var criteria = mock(TrainerTrainingCriteria.class);

        var firstTraining = mock(Training.class);
        var secondTraining = mock(Training.class);

        var firstResponse = mock(TrainingResponseDto.class);
        var secondResponse = mock(TrainingResponseDto.class);

        when(trainerService.findTrainings(TRAINER_USERNAME, criteria))
                .thenReturn(List.of(firstTraining, secondTraining));

        when(trainingMapper.toResponseDto(firstTraining))
                .thenReturn(firstResponse);

        when(trainingMapper.toResponseDto(secondTraining))
                .thenReturn(secondResponse);

        // Act
        var actual = facade.getTrainerTrainings(
                TRAINER_USERNAME,
                criteria
        );

        // Assert
        assertEquals(
                List.of(firstResponse, secondResponse),
                actual
        );

        verify(trainerService).findTrainings(
                TRAINER_USERNAME,
                criteria
        );

        verify(trainingMapper).toResponseDto(firstTraining);
        verify(trainingMapper).toResponseDto(secondTraining);
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainerTrainingsMatch() {

        // Arrange
        var criteria = mock(TrainerTrainingCriteria.class);

        when(trainerService.findTrainings(TRAINER_USERNAME, criteria))
                .thenReturn(List.of());

        // Act
        var actual = facade.getTrainerTrainings(
                TRAINER_USERNAME,
                criteria
        );

        // Assert
        assertNotNull(actual);
        assertTrue(actual.isEmpty());

        verify(trainerService).findTrainings(
                TRAINER_USERNAME,
                criteria
        );

        verifyNoInteractions(trainingMapper);
    }

    @Test
    void rejectsUnauthorizedTrainerSearch() {

        // Arrange
        var criteria = mock(TrainerTrainingCriteria.class);

        when(trainerService.findTrainings(TRAINER_USERNAME, criteria))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        assertThrows(
                AccessDeniedException.class,
                () -> facade.getTrainerTrainings(
                        TRAINER_USERNAME, criteria
                )
        );

        // Assert
        verify(trainerService).findTrainings(
                TRAINER_USERNAME,
                criteria
        );

        verifyNoInteractions(trainingMapper);
    }

    @Test
    void shouldReturnMappedTrainingTypes() {

        // Arrange
        var firstType = mock(TrainingType.class);
        var secondType = mock(TrainingType.class);

        var firstResponse = mock(TrainingTypeResponseDto.class);
        var secondResponse = mock(TrainingTypeResponseDto.class);

        when(trainingTypeService.getTrainingTypes())
                .thenReturn(List.of(firstType, secondType));

        when(trainingTypeMapper.toTrainingTypeResponse(firstType))
                .thenReturn(firstResponse);

        when(trainingTypeMapper.toTrainingTypeResponse(secondType))
                .thenReturn(secondResponse);

        // Act
        var actual = facade.getTrainingTypes();

        // Assert
        assertEquals(
                List.of(firstResponse, secondResponse),
                actual
        );

        verify(trainingTypeService).getTrainingTypes();

        verify(trainingTypeMapper)
                .toTrainingTypeResponse(firstType);

        verify(trainingTypeMapper)
                .toTrainingTypeResponse(secondType);
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainingTypesExist() {

        // Arrange
        when(trainingTypeService.getTrainingTypes())
                .thenReturn(List.of());

        // Act
        var actual = facade.getTrainingTypes();

        // Assert
        assertNotNull(actual);
        assertTrue(actual.isEmpty());

        verify(trainingTypeService).getTrainingTypes();
        verifyNoInteractions(trainingTypeMapper);
    }

    @Test
    void shouldDelegateTrainingCreation() {

        // Arrange
        var request = mock(CreateTrainingRequest.class);
        var training = mock(Training.class);

        when(trainingWebMapper.toTrainingDomain(request))
                .thenReturn(training);

        // Act
        facade.createTraining(request);

        // Assert
        InOrder inOrder = inOrder(
                trainingWebMapper,
                trainingService
        );

        inOrder.verify(trainingWebMapper)
                .toTrainingDomain(request);

        inOrder.verify(trainingService)
                .createTraining(training);
    }

    @Test
    void shouldNotCreateTrainingWhenMappingFails() {

        // Arrange
        var request = mock(CreateTrainingRequest.class);
        var exception = new IllegalArgumentException(
                "Invalid training request"
        );

        when(trainingWebMapper.toTrainingDomain(request))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                IllegalArgumentException.class,
                () -> facade.createTraining(request)
        );

        // Assert
        assertSame(exception, actual);

        verify(trainingWebMapper).toTrainingDomain(request);
        verifyNoInteractions(trainingService);
    }

    @Test
    void shouldPropagateTrainingCreationException() {

        // Arrange
        var request = mock(CreateTrainingRequest.class);
        var training = mock(Training.class);

        var exception = new UserNotFoundException(
                "Trainer not found"
        );

        when(trainingWebMapper.toTrainingDomain(request))
                .thenReturn(training);

        when(trainingService.createTraining(training))
                .thenThrow(exception);

        // Act
        var actual = assertThrows(
                UserNotFoundException.class,
                () -> facade.createTraining(request)
        );

        // Assert
        assertSame(exception, actual);

        verify(trainingWebMapper).toTrainingDomain(request);
        verify(trainingService).createTraining(training);
    }
}