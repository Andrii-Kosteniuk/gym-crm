
package com.training.gym.dao;

import com.training.gym.dto.trainer.TrainerTrainingCriteria;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JpaTrainerRepositoryTest
        extends RepositoryIntegrationSupport {

    private static final String SARA = "sara.connor";
    private static final String JAMES = "james.bond";
    private static final LocalDate FROM =
            LocalDate.of(2026, 11, 1);
    private static final LocalDate TO =
            LocalDate.of(2026, 11, 30);

    @Autowired
    private JpaTrainerRepository repository;

    @Test
    void shouldSaveTrainer() {
        // Arrange
        TrainingType fitness = type("Fitness");
        Trainer sara = Trainer.builder()
                .user(user("Sara", "Connor", SARA))
                .specialization(fitness)
                .build();

        // Act
        Trainer saved = repository.save(sara);
        Long id = saved.getId();
        reload();

        // Assert
        assertNotNull(id);
        assertEquals(
                SARA,
                repository.findById(id)
                        .orElseThrow()
                        .getUser()
                        .getUserName()
        );
    }

    @Test
    void shouldRejectNullTrainerOnSave() {
        // Arrange
        Trainer invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.save(invalid)
        );
    }

    @Test
    void shouldRejectTrainerWithPresetIdOnSave() {
        // Arrange
        Trainer invalid = Trainer.builder()
                .id(100L)
                .user(user("Sara", "Connor", SARA))
                .build();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(invalid)
        );
    }

    @Test
    void shouldUpdateExistingTrainer() {
        // Arrange
        TrainingType fitness = type("Fitness");
        Trainer sara = trainer(SARA, fitness);
        Long id = sara.getId();
        reload();

        Trainer detached =
                repository.findById(id).orElseThrow();

        detached.getUser().setLastName("Connor Updated");
        em.detach(detached);

        // Act
        Trainer actual = repository.update(detached);
        reload();

        // Assert
        assertNotNull(actual);
        assertEquals(
                "Connor Updated",
                repository.findById(id)
                        .orElseThrow()
                        .getUser()
                        .getLastName()
        );
    }

    @Test
    void shouldRejectNullTrainerOnUpdate() {
        // Arrange
        Trainer invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.update(invalid)
        );
    }

    @Test
    void shouldRejectTrainerWithoutIdOnUpdate() {
        // Arrange
        Trainer invalid = Trainer.builder()
                .user(user("Sara", "Connor", SARA))
                .build();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.update(invalid)
        );
    }

    @Test
    void shouldRejectUpdatingUnknownTrainer() {
        // Arrange
        Trainer invalid = Trainer.builder()
                .id(999999L)
                .build();

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.update(invalid)
        );
    }

    @Test
    void shouldFindTrainerById() {
        // Arrange
        Long id = trainer(
                SARA, type("Fitness")
        ).getId();
        reload();

        // Act
        Optional<Trainer> actual =
                repository.findById(id);

        // Assert
        assertTrue(actual.isPresent());
        assertEquals(id, actual.orElseThrow().getId());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        // Arrange
        Long id = 999999L;

        // Act
        Optional<Trainer> actual =
                repository.findById(id);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldFindTrainerByUsername() {
        // Arrange
        trainer(SARA, type("Fitness"));
        reload();

        // Act
        Optional<Trainer> actual =
                repository.findByUserName(SARA);

        // Assert
        assertTrue(actual.isPresent());
        assertEquals(
                "Sara",
                actual.orElseThrow()
                        .getUser()
                        .getFirstName()
        );
    }

    @Test
    void shouldReturnEmptyForUnknownUsername() {
        // Arrange
        String username = "unknown.user";

        // Act
        Optional<Trainer> actual =
                repository.findByUserName(username);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldFindAllTrainers() {
        // Arrange
        TrainingType fitness = type("Fitness");
        trainer(SARA, fitness);
        trainer("jym.kerry", fitness);
        reload();

        // Act
        List<Trainer> actual = repository.findAll();

        // Assert
        assertEquals(2, actual.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainersExist() {
        // Arrange
        // No trainers are persisted.

        // Act
        List<Trainer> actual = repository.findAll();

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenTrainerExists() {
        // Arrange
        Long id = trainer(
                SARA, type("Fitness")
        ).getId();
        reload();

        // Act
        boolean actual = repository.existsById(id);

        // Assert
        assertTrue(actual);
    }

    @Test
    void shouldReturnFalseWhenTrainerDoesNotExist() {
        // Arrange
        Long id = 999999L;

        // Act
        boolean actual = repository.existsById(id);

        // Assert
        assertFalse(actual);
    }

    @Test
    void shouldDeleteTrainerById() {
        // Arrange
        Long id = trainer(
                SARA, type("Fitness")
        ).getId();
        reload();

        // Act
        repository.deleteById(id);
        reload();

        // Assert
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldRejectDeletingUnknownTrainerById() {
        // Arrange
        Long id = 999999L;

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.deleteById(id)
        );
    }

    @Test
    void shouldDeleteTrainerByUsername() {
        // Arrange
        Long id = trainer(
                SARA, type("Fitness")
        ).getId();
        reload();

        // Act
        repository.deleteByUsername(SARA);
        reload();

        // Assert
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldRejectDeletingUnknownTrainerByUsername() {
        // Arrange
        String username = "unknown.user";

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.deleteByUsername(username)
        );
    }

    @Test
    void shouldRejectNullTrainerOnDelete() {
        // Arrange
        Trainer invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.delete(invalid)
        );
    }

    @Test
    void shouldRejectDeletingUnknownTrainerEntity() {
        // Arrange
        Trainer invalid = Trainer.builder()
                .id(999999L)
                .build();

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.delete(invalid)
        );
    }

    @Test
    void shouldActivateTrainer() {
        // Arrange
        Trainer sara = trainer(SARA, type("Fitness"));
        Long id = sara.getId();
        sara.getUser().setActive(false);
        reload();

        // Act
        repository.activateTrainer(
                repository.findById(id).orElseThrow()
        );
        reload();

        // Assert
        assertTrue(
                repository.findById(id)
                        .orElseThrow()
                        .getUser()
                        .getActive()
        );
    }

    @Test
    void shouldDeactivateTrainer() {
        // Arrange
        Long id = trainer(
                SARA, type("Fitness")
        ).getId();
        reload();

        // Act
        repository.deactivateTrainer(
                repository.findById(id).orElseThrow()
        );
        reload();

        // Assert
        assertFalse(
                repository.findById(id)
                        .orElseThrow()
                        .getUser()
                        .getActive()
        );
    }

    @Test
    void shouldRejectNullTrainerOnActivation() {
        // Arrange
        Trainer invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.activateTrainer(invalid)
        );
    }

    @Test
    void shouldRejectNullTrainerOnDeactivation() {
        // Arrange
        Trainer invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.deactivateTrainer(invalid)
        );
    }

    @Test
    void shouldFindUnassignedTrainers() {
        // Arrange
        TrainingType fitness = type("Fitness");
        trainee(JAMES);
        trainer(SARA, fitness);
        trainer("jym.kerry", fitness);
        reload();

        // Act
        List<Trainer> actual =
                repository.findUnassignedTrainers(JAMES);

        // Assert
        assertEquals(2, actual.size());
    }

    @Test
    void shouldExcludeAssignedTrainers() {
        // Arrange
        TrainingType fitness = type("Fitness");
        Trainee james = trainee(JAMES);
        Trainer sara = trainer(SARA, fitness);
        trainer("jym.kerry", fitness);

        james.assignTrainer(sara);
        reload();

        // Act
        List<Trainer> actual =
                repository.findUnassignedTrainers(JAMES);

        // Assert
        assertEquals(1, actual.size());
        assertEquals(
                "jym.kerry",
                actual.getFirst()
                        .getUser()
                        .getUserName()
        );
    }

    @Test
    void shouldRejectBlankUsernameForUnassignedTrainers() {
        // Arrange
        String username = " ";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findUnassignedTrainers(username)
        );
    }

    @Test
    void shouldFindTrainingsWithoutFilters() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        null, null, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByFromDate() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        FROM, null, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByToDate() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        null, TO, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByDateRange() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        FROM, TO, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByTraineeName() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        null, null, "James Bond"
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldReturnEmptyForDifferentTraineeName() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        null, null, "Jym Kerry"
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldIgnoreBlankTraineeNameFilter() {
        // Arrange
        createTrainingFixture();

        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        null, null, " "
                );

        // Act
        List<Training> actual =
                repository.findTrainings(SARA, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        // Arrange
        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        TO.plusDays(1),
                        FROM,
                        null
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findTrainings(SARA, criteria)
        );
    }

    @Test
    void shouldRejectNullTrainingCriteria() {
        // Arrange
        TrainerTrainingCriteria criteria = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.findTrainings(SARA, criteria)
        );
    }

    @Test
    void shouldRejectBlankUsernameForTrainingSearch() {
        // Arrange
        TrainerTrainingCriteria criteria =
                new TrainerTrainingCriteria(
                        null, null, null
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findTrainings(" ", criteria)
        );
    }

    private void createTrainingFixture() {
        // Arrange
        TrainingType fitness = type("Fitness");
        Trainee james = trainee(JAMES);
        Trainer sara = trainer(SARA, fitness);

        training(james, sara, fitness);
        reload();
    }
}
