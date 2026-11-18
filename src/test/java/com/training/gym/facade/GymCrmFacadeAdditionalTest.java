package com.training.gym.facade;

import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.User;
import com.training.gym.domain.mapper.TraineeMapper;
import com.training.gym.domain.mapper.TrainerMapper;
import com.training.gym.domain.mapper.TrainingMapper;
import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.service.*;
import com.training.gym.util.validation.TraineeDtoValidator;
import com.training.gym.util.validation.TrainerDtoValidator;
import com.training.gym.util.validation.TrainingDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymCrmFacadeAdditionalTest {

    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final String TRAINEE_USERNAME = "james.bond";
    private static final String TRAINER_USERNAME = "sara.connor";

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TraineeDtoValidator traineeValidator;
    @Mock
    private TrainerDtoValidator trainerValidator;
    @Mock
    private TrainingDtoValidator trainingValidator;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private ProfileAuthorizationService authorizationService;
    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private GymCrmFacade facade;

    private Credentials traineeCredentials;
    private Credentials trainerCredentials;
    private Trainee jamesBond;
    private Trainer saraConnor;

    @BeforeEach
    void setUp() {
        traineeCredentials = new Credentials(TRAINEE_USERNAME, "Password123");
        trainerCredentials = new Credentials(TRAINER_USERNAME, "Password456");

        User jamesUser = User.builder()
                .id(TRAINEE_ID).firstName("James").lastName("Bond")
                .userName(TRAINEE_USERNAME).active(true).build();
        User saraUser = User.builder()
                .id(TRAINER_ID).firstName("Sara").lastName("Connor")
                .userName(TRAINER_USERNAME).active(true).build();

        jamesBond = Trainee.builder().id(TRAINEE_ID).user(jamesUser).build();
        saraConnor = Trainer.builder().id(TRAINER_ID).user(saraUser).build();
    }

    @Nested
    class GetTraineeByIdTests {
        @Test
        void shouldAuthorizeBeforeReadingAndMappingTrainee() {
            // Arrange
            TraineeResponseDto expected = mock(TraineeResponseDto.class);
            when(traineeService.getTraineeById(TRAINEE_ID)).thenReturn(jamesBond);
            when(traineeMapper.toResponseDto(jamesBond)).thenReturn(expected);

            // Act
            TraineeResponseDto actual = facade.getTraineeById(TRAINEE_ID, traineeCredentials);

            // Assert
            assertSame(expected, actual);
            InOrder order = inOrder(authorizationService, traineeService, traineeMapper);
            order.verify(authorizationService).requireTraineeOwner(traineeCredentials, TRAINEE_ID);
            order.verify(traineeService).getTraineeById(TRAINEE_ID);
            order.verify(traineeMapper).toResponseDto(jamesBond);
            order.verifyNoMoreInteractions();
        }

        @Test
        void shouldNotReadTraineeWhenAuthorizationFails() {
            // Arrange
            doThrow(new AccessDeniedException("Denied"))
                    .when(authorizationService).requireTraineeOwner(traineeCredentials, TRAINEE_ID);

            // Act
            assertThrows(AccessDeniedException.class,
                    () -> facade.getTraineeById(TRAINEE_ID, traineeCredentials));

            // Assert
            verifyNoInteractions(traineeService, traineeMapper);
        }

        @Test
        void shouldNotMapTraineeWhenLookupFails() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenThrow(new IllegalArgumentException("Not found"));

            // Act
            assertThrows(IllegalArgumentException.class,
                    () -> facade.getTraineeById(TRAINEE_ID, traineeCredentials));

            // Assert
            InOrder order = inOrder(authorizationService, traineeService);
            order.verify(authorizationService).requireTraineeOwner(traineeCredentials, TRAINEE_ID);
            order.verify(traineeService).getTraineeById(TRAINEE_ID);
            verifyNoInteractions(traineeMapper);
        }
    }

    @Nested
    class GetTrainerByIdTests {
        @Test
        void shouldAuthorizeBeforeReadingAndMappingTrainer() {
            // Arrange
            TrainerResponseDto expected = mock(TrainerResponseDto.class);
            when(trainerService.getTrainerById(TRAINER_ID)).thenReturn(saraConnor);
            when(trainerMapper.toResponseDto(saraConnor)).thenReturn(expected);

            // Act
            TrainerResponseDto actual = facade.getTrainerById(TRAINER_ID, trainerCredentials);

            // Assert
            assertSame(expected, actual);
            InOrder order = inOrder(authorizationService, trainerService, trainerMapper);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_ID);
            order.verify(trainerService).getTrainerById(TRAINER_ID);
            order.verify(trainerMapper).toResponseDto(saraConnor);
            order.verifyNoMoreInteractions();
        }

        @Test
        void shouldNotReadTrainerWhenAuthorizationFails() {
            // Arrange
            doThrow(new AccessDeniedException("Denied"))
                    .when(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_ID);

            // Act
            assertThrows(AccessDeniedException.class,
                    () -> facade.getTrainerById(TRAINER_ID, trainerCredentials));

            // Assert
            verifyNoInteractions(trainerService, trainerMapper);
        }

        @Test
        void shouldNotMapTrainerWhenLookupFails() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenThrow(new IllegalArgumentException("Not found"));

            // Act
            assertThrows(IllegalArgumentException.class,
                    () -> facade.getTrainerById(TRAINER_ID, trainerCredentials));

            // Assert
            InOrder order = inOrder(authorizationService, trainerService);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_ID);
            order.verify(trainerService).getTrainerById(TRAINER_ID);
            verifyNoInteractions(trainerMapper);
        }
    }

    @Nested
    class GetTrainerByUsernameTests {
        @Test
        void shouldAuthorizeBeforeReadingTrainerByUsername() {
            // Arrange
            TrainerResponseDto expected = mock(TrainerResponseDto.class);
            when(trainerService.findByUserName(TRAINER_USERNAME)).thenReturn(saraConnor);
            when(trainerMapper.toResponseDto(saraConnor)).thenReturn(expected);

            // Act
            TrainerResponseDto actual = facade.getTrainerByUserName(TRAINER_USERNAME, trainerCredentials);

            // Assert
            assertSame(expected, actual);
            InOrder order = inOrder(authorizationService, trainerService, trainerMapper);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_USERNAME);
            order.verify(trainerService).findByUserName(TRAINER_USERNAME);
            order.verify(trainerMapper).toResponseDto(saraConnor);
            order.verifyNoMoreInteractions();
        }

        @Test
        void shouldRejectNullUsernameBeforeCallingDependencies() {
            // Arrange
            String username = null;

            // Act
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> facade.getTrainerByUserName(username, trainerCredentials));

            // Assert
            assertEquals(GymCrmFacade.USERNAME_CAN_NOT_BE_NULL, exception.getMessage());
            verifyNoInteractions(authorizationService, trainerService, trainerMapper);
        }

        @Test
        void shouldNotReadTrainerWhenAuthorizationFails() {
            // Arrange
            doThrow(new AccessDeniedException("Denied"))
                    .when(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_USERNAME);

            // Act
            assertThrows(AccessDeniedException.class,
                    () -> facade.getTrainerByUserName(TRAINER_USERNAME, trainerCredentials));

            // Assert
            verifyNoInteractions(trainerService, trainerMapper);
        }

        @Test
        void shouldNotMapTrainerWhenUsernameLookupFails() {
            // Arrange
            when(trainerService.findByUserName(TRAINER_USERNAME))
                    .thenThrow(new IllegalArgumentException("Not found"));

            // Act
            assertThrows(IllegalArgumentException.class,
                    () -> facade.getTrainerByUserName(TRAINER_USERNAME, trainerCredentials));

            // Assert
            InOrder order = inOrder(authorizationService, trainerService);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_USERNAME);
            order.verify(trainerService).findByUserName(TRAINER_USERNAME);
            verifyNoInteractions(trainerMapper);
        }
    }

    @Nested
    class ChangeTrainerPasswordTests {
        @Test
        void shouldAuthorizeBeforeChangingPassword() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto("OldPassword123", "NewPassword123");

            // Act
            facade.changeTrainerPassword(TRAINER_USERNAME, dto, trainerCredentials);

            // Assert
            InOrder order = inOrder(authorizationService, trainerService);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_USERNAME);
            order.verify(trainerService).changePassword(
                    TRAINER_USERNAME, "OldPassword123", "NewPassword123");
            order.verifyNoMoreInteractions();
        }

        @Test
        void shouldRejectNullPasswordChangeDtoBeforeAuthorization() {
            // Arrange
            ChangePasswordDto dto = null;

            // Act
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> facade.changeTrainerPassword(TRAINER_USERNAME, dto, trainerCredentials));

            // Assert
            assertEquals("Password change DTO is required", exception.getMessage());
            verifyNoInteractions(authorizationService, trainerService);
        }

        @Test
        void shouldNotChangePasswordWhenAuthorizationFails() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto("OldPassword123", "NewPassword123");
            doThrow(new AccessDeniedException("Denied"))
                    .when(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_USERNAME);

            // Act
            assertThrows(AccessDeniedException.class,
                    () -> facade.changeTrainerPassword(TRAINER_USERNAME, dto, trainerCredentials));

            // Assert
            verifyNoInteractions(trainerService);
        }

        @Test
        void shouldPropagatePasswordChangeFailureAfterAuthorization() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto("OldPassword123", "NewPassword123");
            doThrow(new IllegalArgumentException("Invalid password"))
                    .when(trainerService).changePassword(
                            TRAINER_USERNAME, "OldPassword123", "NewPassword123");

            // Act
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> facade.changeTrainerPassword(TRAINER_USERNAME, dto, trainerCredentials));

            // Assert
            assertEquals("Invalid password", exception.getMessage());
            InOrder order = inOrder(authorizationService, trainerService);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_USERNAME);
            order.verify(trainerService).changePassword(
                    TRAINER_USERNAME, "OldPassword123", "NewPassword123");
        }
    }

    @Nested
    class DeactivateTraineeTests {
        @Test
        void shouldDeactivateTraineeOnlyAfterOwnerCheck() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID)).thenReturn(jamesBond);

            // Act
            facade.deActivateTrainee(TRAINEE_ID, traineeCredentials);

            // Assert
            InOrder order = inOrder(authorizationService, traineeService);
            order.verify(authorizationService).requireTraineeOwner(traineeCredentials, TRAINEE_ID);
            order.verify(traineeService).getTraineeById(TRAINEE_ID);
            order.verify(traineeService).deactivateTrainee(jamesBond);
            order.verifyNoMoreInteractions();
        }

        @Test
        void shouldNotReadOrDeactivateTraineeWhenAuthorizationFails() {
            // Arrange
            doThrow(new AccessDeniedException("Denied"))
                    .when(authorizationService).requireTraineeOwner(traineeCredentials, TRAINEE_ID);

            // Act
            assertThrows(AccessDeniedException.class,
                    () -> facade.deActivateTrainee(TRAINEE_ID, traineeCredentials));

            // Assert
            verifyNoInteractions(traineeService);
        }

        @Test
        void shouldNotDeactivateMissingTrainee() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenThrow(new IllegalArgumentException("Not found"));

            // Act
            assertThrows(IllegalArgumentException.class,
                    () -> facade.deActivateTrainee(TRAINEE_ID, traineeCredentials));

            // Assert
            InOrder order = inOrder(authorizationService, traineeService);
            order.verify(authorizationService).requireTraineeOwner(traineeCredentials, TRAINEE_ID);
            order.verify(traineeService).getTraineeById(TRAINEE_ID);
            verify(traineeService, never()).deactivateTrainee(any());
        }
    }

    @Nested
    class DeactivateTrainerTests {
        @Test
        void shouldDeactivateTrainerOnlyAfterOwnerCheck() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID)).thenReturn(saraConnor);

            // Act
            facade.deActivateTrainer(TRAINER_ID, trainerCredentials);

            // Assert
            InOrder order = inOrder(authorizationService, trainerService);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_ID);
            order.verify(trainerService).getTrainerById(TRAINER_ID);
            order.verify(trainerService).deactivateTrainer(saraConnor);
            order.verifyNoMoreInteractions();
        }

        @Test
        void shouldNotReadOrDeactivateTrainerWhenAuthorizationFails() {
            // Arrange
            doThrow(new AccessDeniedException("Denied"))
                    .when(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_ID);

            // Act
            assertThrows(AccessDeniedException.class,
                    () -> facade.deActivateTrainer(TRAINER_ID, trainerCredentials));

            // Assert
            verifyNoInteractions(trainerService);
        }

        @Test
        void shouldNotDeactivateMissingTrainer() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenThrow(new IllegalArgumentException("Not found"));

            // Act
            assertThrows(IllegalArgumentException.class,
                    () -> facade.deActivateTrainer(TRAINER_ID, trainerCredentials));

            // Assert
            InOrder order = inOrder(authorizationService, trainerService);
            order.verify(authorizationService).requireTrainerOwner(trainerCredentials, TRAINER_ID);
            order.verify(trainerService).getTrainerById(TRAINER_ID);
            verify(trainerService, never()).deactivateTrainer(any());
        }
    }
}
