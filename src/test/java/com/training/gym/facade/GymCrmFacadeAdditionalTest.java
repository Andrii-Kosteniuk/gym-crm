
package com.training.gym.facade;

import com.training.gym.dto.auth.ChangePasswordDto;
import com.training.gym.dto.auth.Credentials;
import com.training.gym.dto.trainee.TraineeResponseDto;
import com.training.gym.dto.trainer.TrainerResponseDto;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.mapper.TrainerMapper;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.User;
import com.training.gym.service.ProfileAuthorizationService;
import com.training.gym.service.TraineeService;
import com.training.gym.service.TrainerService;
import com.training.gym.service.TrainingService;
import com.training.gym.validation.TraineeDtoValidator;
import com.training.gym.validation.TrainerDtoValidator;
import com.training.gym.validation.TrainingDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    private GymCrmFacade facade;

    private Credentials traineeCredentials;
    private Credentials trainerCredentials;

    private Trainee jamesBond;
    private Trainer saraConnor;

    @BeforeEach
    void setUp() {
        // Arrange
        traineeCredentials = new Credentials(
                TRAINEE_USERNAME, "Password123"
        );

        trainerCredentials = new Credentials(
                TRAINER_USERNAME, "Password456"
        );

        User jamesUser = User.builder()
                .id(TRAINEE_ID)
                .firstName("James")
                .lastName("Bond")
                .userName(TRAINEE_USERNAME)
                .active(true)
                .build();

        User saraUser = User.builder()
                .id(TRAINER_ID)
                .firstName("Sara")
                .lastName("Connor")
                .userName(TRAINER_USERNAME)
                .active(true)
                .build();

        jamesBond = Trainee.builder()
                .id(TRAINEE_ID)
                .user(jamesUser)
                .build();

        saraConnor = Trainer.builder()
                .id(TRAINER_ID)
                .user(saraUser)
                .build();
    }

    @Nested
    class AuthenticateTraineeTests {

        @Test
        void shouldAuthenticateTrainee() {
            // Arrange
            Credentials credentials = traineeCredentials;

            // Act
            facade.authenticateTrainee(credentials);

            // Assert
            verify(authorizationService)
                    .requireTrainee(credentials);
            verifyNoInteractions(
                    traineeService, trainerService
            );
        }

        @Test
        void shouldPropagateAuthenticationFailure() {
            // Arrange
            Credentials credentials = traineeCredentials;

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTrainee(credentials);

            // Act
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> facade.authenticateTrainee(credentials)
            );

            // Assert
            assertEquals("Access denied", exception.getMessage());
            verifyNoInteractions(
                    traineeService, trainerService
            );
        }

        @Test
        void shouldDelegateNullCredentialsToAuthorizationService() {
            // Arrange
            Credentials credentials = null;

            // Act
            facade.authenticateTrainee(credentials);

            // Assert
            verify(authorizationService)
                    .requireTrainee(null);
        }
    }

    @Nested
    class AuthenticateTrainerTests {

        @Test
        void shouldAuthenticateTrainer() {
            // Arrange
            Credentials credentials = trainerCredentials;

            // Act
            facade.authenticateTrainer(credentials);

            // Assert
            verify(authorizationService)
                    .requireTrainer(credentials);
            verifyNoInteractions(
                    traineeService, trainerService
            );
        }

        @Test
        void shouldPropagateAuthenticationFailure() {
            // Arrange
            Credentials credentials = trainerCredentials;

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTrainer(credentials);

            // Act
            SecurityException exception = assertThrows(
                    SecurityException.class,
                    () -> facade.authenticateTrainer(credentials)
            );

            // Assert
            assertEquals("Access denied", exception.getMessage());
            verifyNoInteractions(
                    traineeService, trainerService
            );
        }

        @Test
        void shouldDelegateNullCredentialsToAuthorizationService() {
            // Arrange
            Credentials credentials = null;

            // Act
            facade.authenticateTrainer(credentials);

            // Assert
            verify(authorizationService)
                    .requireTrainer(null);
        }
    }

    @Nested
    class GetTraineeByIdTests {

        @Test
        void shouldReturnTraineeAfterAuthorization() {
            // Arrange
            TraineeResponseDto expected = mock(
                    TraineeResponseDto.class
            );

            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenReturn(jamesBond);

            when(traineeMapper.toResponseDto(jamesBond))
                    .thenReturn(expected);

            // Act
            TraineeResponseDto actual = facade.getTraineeById(
                    TRAINEE_ID, traineeCredentials
            );

            // Assert
            assertSame(expected, actual);

            var inOrder = inOrder(
                    traineeService,
                    authorizationService,
                    traineeMapper
            );

            inOrder.verify(traineeService)
                    .getTraineeById(TRAINEE_ID);

            inOrder.verify(authorizationService)
                    .requireTraineeOwner(
                            traineeCredentials, TRAINEE_USERNAME
                    );

            inOrder.verify(traineeMapper)
                    .toResponseDto(jamesBond);
        }

        @Test
        void shouldRejectNullTraineeId() {
            // Arrange
            Long id = null;

            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> facade.getTraineeById(
                            id, traineeCredentials
                    )
            );

            // Assert
            assertEquals(
                    GymCrmFacade.ID_CAN_NOT_BE_NULL,
                    exception.getMessage()
            );

            verifyNoInteractions(
                    traineeService,
                    authorizationService,
                    traineeMapper
            );
        }

        @Test
        void shouldNotMapTraineeWhenAuthorizationFails() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenReturn(jamesBond);

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTraineeOwner(
                            traineeCredentials, TRAINEE_USERNAME
                    );

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> facade.getTraineeById(
                            TRAINEE_ID, traineeCredentials
                    )
            );

            verifyNoInteractions(traineeMapper);
        }

        @Test
        void shouldNotAuthorizeWhenTraineeLookupFails() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenThrow(
                            new IllegalArgumentException("Not found")
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> facade.getTraineeById(
                            TRAINEE_ID, traineeCredentials
                    )
            );

            verifyNoInteractions(
                    authorizationService, traineeMapper
            );
        }
    }

    @Nested
    class GetTrainerByIdTests {

        @Test
        void shouldReturnTrainerAfterAuthorization() {
            // Arrange
            TrainerResponseDto expected = mock(
                    TrainerResponseDto.class
            );

            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenReturn(saraConnor);

            when(trainerMapper.toResponseDto(saraConnor))
                    .thenReturn(expected);

            // Act
            TrainerResponseDto actual = facade.getTrainerById(
                    TRAINER_ID, trainerCredentials
            );

            // Assert
            assertSame(expected, actual);

            var inOrder = inOrder(
                    trainerService,
                    authorizationService,
                    trainerMapper
            );

            inOrder.verify(trainerService)
                    .getTrainerById(TRAINER_ID);

            inOrder.verify(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            inOrder.verify(trainerMapper)
                    .toResponseDto(saraConnor);
        }

        @Test
        void shouldRejectNullTrainerId() {
            // Arrange
            Long id = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> facade.getTrainerById(
                            id, trainerCredentials
                    )
            );

            verifyNoInteractions(
                    trainerService,
                    authorizationService,
                    trainerMapper
            );
        }

        @Test
        void shouldNotMapTrainerWhenAuthorizationFails() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenReturn(saraConnor);

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> facade.getTrainerById(
                            TRAINER_ID, trainerCredentials
                    )
            );

            verifyNoInteractions(trainerMapper);
        }

        @Test
        void shouldNotAuthorizeWhenTrainerLookupFails() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenThrow(
                            new IllegalArgumentException("Not found")
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> facade.getTrainerById(
                            TRAINER_ID, trainerCredentials
                    )
            );

            verifyNoInteractions(
                    authorizationService, trainerMapper
            );
        }
    }

    @Nested
    class GetTrainerByUsernameTests {

        @Test
        void shouldReturnTrainerByUsername() {
            // Arrange
            TrainerResponseDto expected = mock(
                    TrainerResponseDto.class
            );

            when(trainerService.findByUserName(TRAINER_USERNAME))
                    .thenReturn(saraConnor);

            when(trainerMapper.toResponseDto(saraConnor))
                    .thenReturn(expected);

            // Act
            TrainerResponseDto actual =
                    facade.getTrainerByUserName(
                            TRAINER_USERNAME, trainerCredentials
                    );

            // Assert
            assertSame(expected, actual);

            var inOrder = inOrder(
                    trainerService,
                    authorizationService,
                    trainerMapper
            );

            inOrder.verify(trainerService)
                    .findByUserName(TRAINER_USERNAME);

            inOrder.verify(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            inOrder.verify(trainerMapper)
                    .toResponseDto(saraConnor);
        }

        @Test
        void shouldRejectNullUsername() {
            // Arrange
            String username = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> facade.getTrainerByUserName(
                            username, trainerCredentials
                    )
            );

            verifyNoInteractions(
                    trainerService,
                    authorizationService,
                    trainerMapper
            );
        }

        @Test
        void shouldNotMapTrainerWhenAuthorizationFails() {
            // Arrange
            when(trainerService.findByUserName(TRAINER_USERNAME))
                    .thenReturn(saraConnor);

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> facade.getTrainerByUserName(
                            TRAINER_USERNAME, trainerCredentials
                    )
            );

            verifyNoInteractions(trainerMapper);
        }

        @Test
        void shouldNotAuthorizeWhenUsernameLookupFails() {
            // Arrange
            when(trainerService.findByUserName(TRAINER_USERNAME))
                    .thenThrow(
                            new IllegalArgumentException("Not found")
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> facade.getTrainerByUserName(
                            TRAINER_USERNAME, trainerCredentials
                    )
            );

            verifyNoInteractions(
                    authorizationService, trainerMapper
            );
        }
    }

    @Nested
    class ChangeTrainerPasswordTests {

        @Test
        void shouldChangeTrainerPasswordAfterAuthorization() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(
                    "OldPassword123", "NewPassword123"
            );

            // Act
            facade.changeTrainerPassword(
                    TRAINER_USERNAME, dto, trainerCredentials
            );

            // Assert
            var inOrder = inOrder(
                    authorizationService, trainerService
            );

            inOrder.verify(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            inOrder.verify(trainerService)
                    .changePassword(
                            TRAINER_USERNAME,
                            "OldPassword123",
                            "NewPassword123"
                    );
        }

        @Test
        void shouldRejectNullPasswordChangeDto() {
            // Arrange
            ChangePasswordDto dto = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> facade.changeTrainerPassword(
                            TRAINER_USERNAME, dto, trainerCredentials
                    )
            );

            verifyNoInteractions(
                    authorizationService, trainerService
            );
        }

        @Test
        void shouldNotChangePasswordWhenAuthorizationFails() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(
                    "OldPassword123", "NewPassword123"
            );

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> facade.changeTrainerPassword(
                            TRAINER_USERNAME, dto, trainerCredentials
                    )
            );

            verifyNoInteractions(trainerService);
        }

        @Test
        void shouldPropagatePasswordChangeFailure() {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(
                    "OldPassword123", "NewPassword123"
            );

            doThrow(new IllegalArgumentException("Invalid password"))
                    .when(trainerService)
                    .changePassword(
                            TRAINER_USERNAME,
                            "OldPassword123",
                            "NewPassword123"
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> facade.changeTrainerPassword(
                            TRAINER_USERNAME, dto, trainerCredentials
                    )
            );

            verify(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );
        }
    }

    @Nested
    class DeactivateTraineeTests {

        @Test
        void shouldDeactivateTraineeAfterAuthorization() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenReturn(jamesBond);

            // Act
            facade.deActivateTrainee(
                    TRAINEE_ID, traineeCredentials
            );

            // Assert
            var inOrder = inOrder(
                    traineeService, authorizationService
            );

            inOrder.verify(traineeService)
                    .getTraineeById(TRAINEE_ID);

            inOrder.verify(authorizationService)
                    .requireTraineeOwner(
                            traineeCredentials, TRAINEE_USERNAME
                    );

            inOrder.verify(traineeService)
                    .deactivateTrainee(jamesBond);
        }

        @Test
        void shouldRejectNullTraineeId() {
            // Arrange
            Long id = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> facade.deActivateTrainee(
                            id, traineeCredentials
                    )
            );

            verifyNoInteractions(
                    traineeService, authorizationService
            );
        }

        @Test
        void shouldNotDeactivateUnauthorizedTrainee() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenReturn(jamesBond);

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTraineeOwner(
                            traineeCredentials, TRAINEE_USERNAME
                    );

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> facade.deActivateTrainee(
                            TRAINEE_ID, traineeCredentials
                    )
            );

            verify(traineeService, never())
                    .deactivateTrainee(any());
        }

        @Test
        void shouldNotDeactivateMissingTrainee() {
            // Arrange
            when(traineeService.getTraineeById(TRAINEE_ID))
                    .thenThrow(
                            new IllegalArgumentException("Not found")
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> facade.deActivateTrainee(
                            TRAINEE_ID, traineeCredentials
                    )
            );

            verifyNoInteractions(authorizationService);
            verify(traineeService, never())
                    .deactivateTrainee(any());
        }
    }

    @Nested
    class DeactivateTrainerTests {

        @Test
        void shouldDeactivateTrainerAfterAuthorization() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenReturn(saraConnor);

            // Act
            facade.deActivateTrainer(
                    TRAINER_ID, trainerCredentials
            );

            // Assert
            var inOrder = inOrder(
                    trainerService, authorizationService
            );

            inOrder.verify(trainerService)
                    .getTrainerById(TRAINER_ID);

            inOrder.verify(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            inOrder.verify(trainerService)
                    .deactivateTrainer(saraConnor);
        }

        @Test
        void shouldRejectNullTrainerId() {
            // Arrange
            Long id = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> facade.deActivateTrainer(
                            id, trainerCredentials
                    )
            );

            verifyNoInteractions(
                    trainerService, authorizationService
            );
        }

        @Test
        void shouldNotDeactivateUnauthorizedTrainer() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenReturn(saraConnor);

            doThrow(new SecurityException("Access denied"))
                    .when(authorizationService)
                    .requireTrainerOwner(
                            trainerCredentials, TRAINER_USERNAME
                    );

            // Act & Assert
            assertThrows(
                    SecurityException.class,
                    () -> facade.deActivateTrainer(
                            TRAINER_ID, trainerCredentials
                    )
            );

            verify(trainerService, never())
                    .deactivateTrainer(any());
        }

        @Test
        void shouldNotDeactivateMissingTrainer() {
            // Arrange
            when(trainerService.getTrainerById(TRAINER_ID))
                    .thenThrow(
                            new IllegalArgumentException("Not found")
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> facade.deActivateTrainer(
                            TRAINER_ID, trainerCredentials
                    )
            );

            verifyNoInteractions(authorizationService);
            verify(trainerService, never())
                    .deactivateTrainer(any());
        }
    }
}
