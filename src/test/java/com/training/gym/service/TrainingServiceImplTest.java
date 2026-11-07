package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dto.training.CreateTrainingDto;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.mapper.TrainingMapper;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import com.training.gym.util.IdGenerator;
import com.training.gym.validation.TrainingDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final Long TRAINING_ID = 1L;
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 1L;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private TrainingDtoValidator dtoValidator;


    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;
    private CreateTrainingDto createDto;

    @BeforeEach
    void setUp() {
        training = new Training();

        createDto = new CreateTrainingDto(
                TRAINEE_ID,
                TRAINER_ID,
                "Strength Training",
                new TrainingType(1L, "Strength"),
                LocalDate.of(2026, 11, 10),
                Duration.ofMinutes(60)
        );
    }

    @Nested
    class CreateTests {

        @Test
        void shouldCreateTrainingSuccessfully() {
            // Arrange
            assertEquals(TRAINEE_ID, createDto.traineeId());
            assertEquals(TRAINER_ID, createDto.trainerId());

            when(traineeRepository.existsById(TRAINEE_ID))
                    .thenReturn(true);

            when(trainerRepository.existsById(TRAINER_ID))
                    .thenReturn(true);

            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            when(trainingRepository.save(training))
                    .thenReturn(training);

            // Act
            Training result = trainingService.createTraining(createDto);

            // Assert
            assertNotNull(result);
            assertSame(training, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(dtoValidator).validateCreate(createDto);
            verify(traineeRepository).existsById(TRAINEE_ID);
            verify(trainerRepository).existsById(TRAINER_ID);
            verify(trainingMapper).toTraining(createDto);
            verify(idGenerator).generate();
            verify(trainingRepository).save(training);
        }

        @Test
        void shouldAssignGeneratedIdBeforeSavingTraining() {
            // Arrange
            existingTrainingParticipants();

            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            when(trainingRepository.save(any(Training.class)))
                    .thenAnswer(invocation -> {
                        Training entity = invocation.getArgument(0);

                        assertEquals(TRAINING_ID, entity.getId());

                        return entity;
                    });

            // Act
            Training result = trainingService.createTraining(createDto);

            // Assert
            assertEquals(TRAINING_ID, result.getId());

            ArgumentCaptor<Training> captor =
                    ArgumentCaptor.forClass(Training.class);

            verify(trainingRepository).save(captor.capture());

            assertSame(training, captor.getValue());
            assertEquals(TRAINING_ID, captor.getValue().getId());
        }

        @Test
        void shouldRejectNullCreateTrainingDto() {

            doThrow(new IllegalArgumentException("CreateTrainingDto must not be null"))
                    .when(dtoValidator)
                    .validateCreate(isNull());

            // Act
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> trainingService.createTraining(null)
            );

            // Assert
            assertEquals(
                    "CreateTrainingDto must not be null",
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectTrainingWhenTraineeDoesNotExist() {
            // Arrange
            when(traineeRepository.existsById(TRAINEE_ID)).thenReturn(false);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> trainingService.createTraining(createDto)
            );

            // Assert
            assertEquals(
                    "Trainee with id %d not found".formatted(TRAINEE_ID),
                    exception.getMessage()
            );

            verify(traineeRepository).existsById(TRAINEE_ID);

            verifyNoInteractions(
                    trainerRepository,
                    trainingMapper,
                    idGenerator,
                    trainingRepository
            );
        }

        @Test
        void shouldRejectTrainingWhenTrainerDoesNotExist() {
            // Arrange
            when(traineeRepository.existsById(TRAINEE_ID))

                    .thenReturn(true);
            when(trainerRepository.existsById(TRAINER_ID))
                    .thenReturn(false);

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> trainingService.createTraining(createDto)
            );

            // Assert
            assertEquals(
                    "Trainer with id " + TRAINER_ID + " not found",
                    exception.getMessage()
            );

            verify(trainerRepository).existsById(TRAINER_ID);
            verify(dtoValidator).validateCreate(createDto);

            verifyNoInteractions(
                    trainingMapper,
                    idGenerator,
                    trainingRepository
            );
        }

        @Test
        void shouldReturnEntityReturnedByRepository() {
            // Arrange
            Training persistedTraining = new Training();
            persistedTraining.setId(TRAINING_ID);

            when(traineeRepository.existsById(TRAINEE_ID))
                    .thenReturn(true);

            when(trainerRepository.existsById(TRAINER_ID))
                    .thenReturn(true);

            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            when(trainingRepository.save(training))
                    .thenReturn(persistedTraining);

            // Act
            Training result = trainingService.createTraining(createDto);

            // Assert
            assertSame(persistedTraining, result);

            verify(trainingRepository).save(training);
        }

        @Test
        void shouldPropagateRepositoryExceptionDuringCreation() {
            // Arrange

            existingTrainingParticipants();

            when(trainingMapper.toTraining(createDto))
                    .thenReturn(training);

            when(idGenerator.generate())
                    .thenReturn(TRAINING_ID);

            RuntimeException repositoryException =
                    new RuntimeException("Storage failure");

            when(trainingRepository.save(training))
                    .thenThrow(repositoryException);

            // Act
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> trainingService.createTraining(createDto)
            );

            // Assert
            assertSame(repositoryException, exception);

            verify(trainingRepository).save(training);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnTrainingWhenIdExists() {
            // Arrange
            training.setId(TRAINING_ID);

            when(trainingRepository.findById(TRAINING_ID))
                    .thenReturn(Optional.of(training));

            // Act
            Training result =
                    trainingService.getTrainingById(TRAINING_ID);

            // Assert
            assertNotNull(result);
            assertSame(training, result);
            assertEquals(TRAINING_ID, result.getId());

            verify(trainingRepository).findById(TRAINING_ID);
        }

        @Test
        void shouldThrowExceptionWhenTrainingDoesNotExist() {
            // Arrange
            Long nonexistentId = 999L;

            when(trainingRepository.findById(nonexistentId))
                    .thenReturn(Optional.empty());

            // Act
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> trainingService.getTrainingById(nonexistentId)
            );

            // Assert
            assertEquals(
                    "Training with id 999 not found",
                    exception.getMessage()
            );

            verify(trainingRepository).findById(nonexistentId);
        }

        @Test
        void shouldReturnCorrectTrainingForRequestedId() {
            // Arrange
            Long requestedId = 5L;

            Training expectedTraining = new Training();
            expectedTraining.setId(requestedId);

            when(trainingRepository.findById(requestedId))
                    .thenReturn(Optional.of(expectedTraining));

            // Act
            Training result =
                    trainingService.getTrainingById(requestedId);

            // Assert
            assertSame(expectedTraining, result);
            assertEquals(requestedId, result.getId());

            verify(trainingRepository).findById(requestedId);
        }

        @Test
        void shouldNotCallOtherDependenciesWhenRetrievingTraining() {
            // Arrange
            training.setId(TRAINING_ID);

            when(trainingRepository.findById(TRAINING_ID))
                    .thenReturn(Optional.of(training));

            // Act
            Training result =
                    trainingService.getTrainingById(TRAINING_ID);

            // Assert
            assertNotNull(result);

            verify(trainingRepository).findById(TRAINING_ID);
            verifyNoInteractions(idGenerator, trainingMapper);
        }
    }

    private void existingTrainingParticipants() {
        when(traineeRepository.existsById(TRAINEE_ID))
                .thenReturn(true);

        when(trainerRepository.existsById(TRAINER_ID))
                .thenReturn(true);
    }
}