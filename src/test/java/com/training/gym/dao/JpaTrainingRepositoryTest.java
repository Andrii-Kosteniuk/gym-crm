
package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JpaTrainingRepositoryTest
        extends RepositoryIntegrationSupport {

    private static final String JAMES = "james.bond";
    private static final String SARA = "sara.connor";
    private static final String JYM = "jym.kerry";

    private static final LocalDate EARLY =
            LocalDate.of(2026, 10, 10);

    private static final LocalDate MIDDLE =
            LocalDate.of(2026, 11, 10);

    private static final LocalDate LATE =
            LocalDate.of(2026, 12, 10);

    @Autowired
    private JpaTrainingRepository repository;

    private TrainingType fitness;
    private TrainingType yoga;

    private Trainee james;
    private Trainee jym;

    private Trainer sara;
    private Trainer otherTrainer;

    @BeforeEach
    void setUp() {
        // Arrange
        fitness = type("Fitness");
        yoga = type("Yoga");

        james = trainee(JAMES);
        jym = trainee(JYM);

        sara = trainer(
                SARA, fitness
        );

        otherTrainer = trainer(
                "other.trainer", yoga
        );

        reload();
    }

    @Nested
    class SaveTests {

        @Test
        void shouldPersistTrainingAndGenerateId() {
            // Arrange
            Training entity = newTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            Training saved = repository.save(entity);
            Long id = saved.getId();
            reload();

            // Assert
            assertNotNull(id);
            assertSame(entity, saved);
            assertTrue(repository.existsById(id));
        }

        @Test
        void shouldPersistTrainingRelationships() {
            // Arrange
            Training entity = newTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            Long id = repository.save(entity).getId();
            reload();

            // Assert
            Training actual =
                    repository.findById(id).orElseThrow();

            assertAll(
                    () -> assertEquals(
                            james.getId(),
                            actual.getTrainee().getId()
                    ),
                    () -> assertEquals(
                            sara.getId(),
                            actual.getTrainer().getId()
                    ),
                    () -> assertEquals(
                            fitness.getId(),
                            actual.getTrainingType().getId()
                    )
            );
        }

        @Test
        void shouldPersistDurationInMinutes() {
            // Arrange
            Training entity = newTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            Long id = repository.save(entity).getId();
            reload();

            // Assert
            Training actual =
                    repository.findById(id).orElseThrow();

            assertEquals(
                    Duration.ofMinutes(45),
                    actual.getTrainingDuration()
            );
        }

        @Test
        void shouldRejectNullTraining() {
            // Arrange
            Training entity = null;

            // Act
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> repository.save(entity)
            );

            // Assert
            assertEquals(
                    JpaTrainingRepository.TRAINING_MUST_NOT_BE_NULL,
                    exception.getMessage()
            );
        }

        @Test
        void shouldRejectTrainingWithPresetId() {
            // Arrange
            Training entity = newTraining(
                    james, sara, fitness, MIDDLE
            );
            entity.setId(100L);

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.save(entity)
            );
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldFindExistingTraining() {
            // Arrange
            Long id = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            Optional<Training> result =
                    repository.findById(id);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(id, result.orElseThrow().getId());
        }

        @Test
        void shouldReturnEmptyForUnknownId() {
            // Arrange
            Long id = 999999L;

            // Act
            Optional<Training> result =
                    repository.findById(id);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldRejectNullId() {
            // Arrange
            Long id = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> repository.findById(id)
            );
        }
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnEmptyListWithoutTrainings() {
            // Arrange
            // No training records have been created.

            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnAllTrainings() {
            // Arrange
            persistTraining(james, sara, fitness, EARLY);
            persistTraining(jym, sara, fitness, MIDDLE);

            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        void shouldOrderByDateDescending() {
            // Arrange
            Long earlyId = persistTraining(
                    james, sara, fitness, EARLY
            );
            Long lateId = persistTraining(
                    james, sara, fitness, LATE
            );

            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertEquals(
                    List.of(lateId, earlyId),
                    result.stream().map(Training::getId).toList()
            );
        }

        @Test
        void shouldOrderSameDateByIdDescending() {
            // Arrange
            Long firstId = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            Long secondId = persistTraining(
                    jym, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository.findAll();

            // Assert
            assertEquals(
                    List.of(secondId, firstId),
                    result.stream().map(Training::getId).toList()
            );
        }
    }

    @Nested
    class ExistsByIdTests {

        @Test
        void shouldReturnTrueWhenTrainingExists() {
            // Arrange
            Long id = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            boolean result = repository.existsById(id);

            // Assert
            assertTrue(result);
        }

        @Test
        void shouldReturnFalseWhenTrainingDoesNotExist() {
            // Arrange
            Long id = 999999L;

            // Act
            boolean result = repository.existsById(id);

            // Assert
            assertFalse(result);
        }

        @Test
        void shouldRejectNullId() {
            // Arrange
            Long id = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> repository.existsById(id)
            );
        }
    }

    @Nested
    class DeleteByIdTests {

        @Test
        void shouldDeleteExistingTraining() {
            // Arrange
            Long id = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            repository.deleteById(id);
            reload();

            // Assert
            assertFalse(repository.existsById(id));
        }

        @Test
        void shouldRejectUnknownTraining() {
            // Arrange
            Long id = 999999L;

            // Act & Assert
            assertThrows(
                    EntityNotFoundException.class,
                    () -> repository.deleteById(id)
            );
        }

        @Test
        void shouldRejectNullId() {
            // Arrange
            Long id = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> repository.deleteById(id)
            );
        }

        @Test
        void shouldPreserveRelatedProfiles() {
            // Arrange
            Long id = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            Long traineeId = james.getId();
            Long trainerId = sara.getId();

            // Act
            repository.deleteById(id);
            reload();

            // Assert
            assertAll(
                    () -> assertNotNull(
                            em.find(Trainee.class, traineeId)
                    ),
                    () -> assertNotNull(
                            em.find(Trainer.class, trainerId)
                    )
            );
        }
    }

    @Nested
    class TraineeTrainingSearchTests {

        @Test
        void shouldReturnTrainingsWithoutOptionalFilters() {
            // Arrange
            persistTraining(james, sara, fitness, MIDDLE);
            persistTraining(james, sara, fitness, LATE);
            persistTraining(jym, sara, fitness, EARLY);

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, null, null, null
                            )
                    );

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        void shouldFilterByFromDate() {
            // Arrange
            persistTraining(james, sara, fitness, EARLY);
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    MIDDLE, null, null, null
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldFilterByToDate() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, EARLY
            );
            persistTraining(james, sara, fitness, LATE);

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, EARLY, null, null
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldIncludeBothDateBoundaries() {
            // Arrange
            Long first = persistTraining(
                    james, sara, fitness, EARLY
            );
            Long second = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            persistTraining(james, sara, fitness, LATE);

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    EARLY, MIDDLE, null, null
                            )
                    );

            // Assert
            assertIds(result, second, first);
        }

        @Test
        void shouldTrimTrainerName() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, null,
                                    "  Sara Connor  ", null
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldFilterByTrainingType() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            persistTraining(
                    james, otherTrainer, yoga, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, null, null, "Fitness"
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldTrimTrainingType() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, null, null, " Fitness "
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldIgnoreBlankOptionalFilters() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, null, "  ", " "
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldApplyAllFiltersTogether() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            persistTraining(james, sara, fitness, EARLY);
            persistTraining(james, otherTrainer, yoga, MIDDLE);
            persistTraining(jym, sara, fitness, MIDDLE);

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    MIDDLE,
                                    LATE,
                                    "Sara Connor",
                                    "Fitness"
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldReturnEmptyForUnknownTrainee() {
            // Arrange
            persistTraining(james, sara, fitness, MIDDLE);

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            "unknown.user",
                            traineeCriteria(
                                    null, null, null, null
                            )
                    );

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldRejectNullUsername() {
            // Arrange
            String username = null;

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findByTraineeUsername(
                            username,
                            traineeCriteria(
                                    null, null, null, null
                            )
                    )
            );
        }

        @Test
        void shouldRejectBlankUsername() {
            // Arrange
            String username = "  ";

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findByTraineeUsername(
                            username,
                            traineeCriteria(
                                    null, null, null, null
                            )
                    )
            );
        }

        @Test
        void shouldRejectNullCriteria() {
            // Arrange
            TraineeTrainingCriteria criteria = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> repository.findByTraineeUsername(
                            JAMES, criteria
                    )
            );
        }

        @Test
        void shouldRejectReversedDateRange() {
            // Arrange
            TraineeTrainingCriteria criteria =
                    traineeCriteria(
                            LATE, EARLY, null, null
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findByTraineeUsername(
                            JAMES, criteria
                    )
            );
        }

        @Test
        void shouldOrderResultsByDateThenIdDescending() {
            // Arrange
            Long early = persistTraining(
                    james, sara, fitness, EARLY
            );
            Long firstMiddle = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            Long secondMiddle = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTraineeUsername(
                            JAMES, traineeCriteria(
                                    null, null, null, null
                            )
                    );

            // Assert
            assertIds(
                    result,
                    secondMiddle,
                    firstMiddle,
                    early
            );
        }
    }

    @Nested
    class TrainerTrainingSearchTests {

        @Test
        void shouldReturnTrainingsWithoutFilters() {
            // Arrange
            persistTraining(james, sara, fitness, EARLY);
            persistTraining(jym, sara, fitness, MIDDLE);
            persistTraining(james, otherTrainer, yoga, LATE);

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    null, null, null
                            )
                    );

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        void shouldFilterByFromDate() {
            // Arrange
            persistTraining(james, sara, fitness, EARLY);
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    MIDDLE, null, null
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldFilterByToDate() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, EARLY
            );
            persistTraining(james, sara, fitness, LATE);

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    null, EARLY, null
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldIncludeBothDateBoundaries() {
            // Arrange
            Long early = persistTraining(
                    james, sara, fitness, EARLY
            );
            Long middle = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            persistTraining(james, sara, fitness, LATE);

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    EARLY, MIDDLE, null
                            )
                    );

            // Assert
            assertIds(result, middle, early);
        }

        @Test
        void shouldTrimTraineeName() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    null, null, " James Bond "
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldIgnoreBlankTraineeName() {
            // Arrange
            Long expected = persistTraining(
                    james, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    null, null, "  "
                            )
                    );

            // Assert
            assertIds(result, expected);
        }

        @Test
        void shouldReturnEmptyForUnknownTrainer() {
            // Arrange
            persistTraining(james, sara, fitness, MIDDLE);

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            "unknown.trainer",
                            trainerCriteria(
                                    null, null, null
                            )
                    );

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldRejectNullUsername() {
            // Arrange
            String username = null;

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findByTrainerUsername(
                            username,
                            trainerCriteria(
                                    null, null, null
                            )
                    )
            );
        }

        @Test
        void shouldRejectBlankUsername() {
            // Arrange
            String username = " ";

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findByTrainerUsername(
                            username,
                            trainerCriteria(
                                    null, null, null
                            )
                    )
            );
        }

        @Test
        void shouldRejectNullCriteria() {
            // Arrange
            TrainerTrainingCriteria criteria = null;

            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> repository.findByTrainerUsername(
                            SARA, criteria
                    )
            );
        }

        @Test
        void shouldRejectReversedDateRange() {
            // Arrange
            TrainerTrainingCriteria criteria =
                    trainerCriteria(
                            LATE, EARLY, null
                    );

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.findByTrainerUsername(
                            SARA, criteria
                    )
            );
        }

        @Test
        void shouldOrderResultsByDateThenIdDescending() {
            // Arrange
            Long early = persistTraining(
                    james, sara, fitness, EARLY
            );
            Long firstMiddle = persistTraining(
                    james, sara, fitness, MIDDLE
            );
            Long secondMiddle = persistTraining(
                    jym, sara, fitness, MIDDLE
            );

            // Act
            List<Training> result = repository
                    .findByTrainerUsername(
                            SARA, trainerCriteria(
                                    null, null, null
                            )
                    );

            // Assert
            assertIds(
                    result,
                    secondMiddle,
                    firstMiddle,
                    early
            );
        }
    }

    private Training newTraining(
            Trainee trainee,
            Trainer trainer,
            TrainingType type,
            LocalDate date
    ) {
        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .trainingName("Training session")
                .trainingDate(date)
                .trainingDuration(Duration.ofMinutes(45))
                .build();
    }

    private Long persistTraining(
            Trainee trainee,
            Trainer trainer,
            TrainingType type,
            LocalDate date
    ) {
        Training entity = newTraining(
                trainee, trainer, type, date
        );

        repository.save(entity);
        Long id = entity.getId();

        reload();

        return id;
    }

    private TraineeTrainingCriteria traineeCriteria(
            LocalDate from,
            LocalDate to,
            String trainerName,
            String trainingType
    ) {
        return new TraineeTrainingCriteria(
                from, to, trainerName, trainingType
        );
    }

    private TrainerTrainingCriteria trainerCriteria(
            LocalDate from,
            LocalDate to,
            String traineeName
    ) {
        return new TrainerTrainingCriteria(
                from, to, traineeName
        );
    }

    private void assertIds(
            List<Training> actual,
            Long... expectedIds
    ) {
        assertEquals(
                List.of(expectedIds),
                actual.stream()
                        .map(Training::getId)
                        .toList()
        );
    }
}
