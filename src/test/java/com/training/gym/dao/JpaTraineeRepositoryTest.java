
package com.training.gym.dao;

import com.training.gym.dto.trainee.TraineeTrainingCriteria;
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

class JpaTraineeRepositoryTest
        extends RepositoryIntegrationSupport {

    private static final String JAMES = "james.bond";
    private static final String SARA = "sara.connor";
    private static final LocalDate FROM =
            LocalDate.of(2026, 11, 1);
    private static final LocalDate TO =
            LocalDate.of(2026, 11, 30);

    @Autowired
    private JpaTraineeRepository repository;

    @Test
    void shouldSaveTraineeWithGeneratedId() {
        // Arrange
        Trainee james = Trainee.builder()
                .user(user("James", "Bond", JAMES))
                .dateOfBirth(LocalDate.of(1990, 4, 13))
                .build();

        // Act
        Trainee saved = repository.save(james);
        Long id = saved.getId();
        reload();

        // Assert
        assertNotNull(id);
        assertEquals(id, saved.getId());
        assertEquals(
                JAMES,
                repository.findById(id)
                        .orElseThrow()
                        .getUser()
                        .getUserName()
        );
    }

    @Test
    void shouldRejectNullTraineeOnSave() {
        // Arrange
        Trainee invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.save(invalid)
        );
    }

    @Test
    void shouldRejectTraineeWithExistingIdOnSave() {
        // Arrange
        Trainee james = Trainee.builder()
                .id(100L)
                .user(user("James", "Bond", JAMES))
                .build();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(james)
        );
    }

    @Test
    void shouldUpdateExistingTrainee() {
        // Arrange
        Trainee james = trainee(JAMES);
        Long id = james.getId();
        reload();

        Trainee detached = repository.findById(id)
                .orElseThrow();

        detached.setDateOfBirth(
                LocalDate.of(1991, 5, 14)
        );

        em.detach(detached);

        // Act
        Trainee updated = repository.update(detached);
        reload();

        // Assert
        assertNotNull(updated);
        assertEquals(
                LocalDate.of(1991, 5, 14),
                repository.findById(id)
                        .orElseThrow()
                        .getDateOfBirth()
        );
    }

    @Test
    void shouldRejectNullTraineeOnUpdate() {
        // Arrange
        Trainee invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.update(invalid)
        );
    }

    @Test
    void shouldRejectTraineeWithoutIdOnUpdate() {
        // Arrange
        Trainee james = Trainee.builder()
                .user(user("James", "Bond", JAMES))
                .build();

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.update(james)
        );
    }

    @Test
    void shouldFindTraineeById() {
        // Arrange
        Long id = trainee(JAMES).getId();
        reload();

        // Act
        Optional<Trainee> actual =
                repository.findById(id);

        // Assert
        assertTrue(actual.isPresent());
        assertEquals(JAMES,
                actual.orElseThrow()
                        .getUser()
                        .getUserName());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        // Arrange
        Long id = 999999L;

        // Act
        Optional<Trainee> actual =
                repository.findById(id);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldRejectNullIdOnFindById() {
        // Arrange
        Long id = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.findById(id)
        );
    }

    @Test
    void shouldFindTraineeByUsername() {
        // Arrange
        trainee(JAMES);
        reload();

        // Act
        Optional<Trainee> actual =
                repository.findByUserName(JAMES);

        // Assert
        assertTrue(actual.isPresent());
        assertEquals(
                "James",
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
        Optional<Trainee> actual =
                repository.findByUserName(username);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldRejectNullUsername() {
        // Arrange
        String username = null;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findByUserName(username)
        );
    }

    @Test
    void shouldRejectBlankUsername() {
        // Arrange
        String username = "   ";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findByUserName(username)
        );
    }

    @Test
    void shouldReturnAllTrainees() {
        // Arrange
        trainee(JAMES);
        trainee("jym.kerry");
        reload();

        // Act
        List<Trainee> actual = repository.findAll();

        // Assert
        assertEquals(2, actual.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoTraineesExist() {
        // Arrange
        // Test database contains no trainees.

        // Act
        List<Trainee> actual = repository.findAll();

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenTraineeExists() {
        // Arrange
        Long id = trainee(JAMES).getId();
        reload();

        // Act
        boolean actual = repository.existsById(id);

        // Assert
        assertTrue(actual);
    }

    @Test
    void shouldReturnFalseWhenTraineeDoesNotExist() {
        // Arrange
        Long id = 999999L;

        // Act
        boolean actual = repository.existsById(id);

        // Assert
        assertFalse(actual);
    }

    @Test
    void shouldDeleteTraineeById() {
        // Arrange
        Long id = trainee(JAMES).getId();
        reload();

        // Act
        repository.deleteById(id);
        reload();

        // Assert
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldRejectDeletingUnknownTraineeById() {
        // Arrange
        Long id = 999999L;

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.deleteById(id)
        );
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        // Arrange
        Long id = trainee(JAMES).getId();
        reload();

        // Act
        repository.deleteByUsername(JAMES);
        reload();

        // Assert
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void shouldRejectDeletingUnknownUsername() {
        // Arrange
        String username = "unknown.user";

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.deleteByUsername(username)
        );
    }

    @Test
    void shouldRejectDeletingBlankUsername() {
        // Arrange
        String username = " ";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.deleteByUsername(username)
        );
    }

    @Test
    void shouldRejectNullTraineeOnDelete() {
        // Arrange
        Trainee invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.delete(invalid)
        );
    }

    @Test
    void shouldActivateTrainee() {
        // Arrange
        Trainee james = trainee(JAMES);
        Long id = james.getId();
        james.getUser().setActive(false);
        reload();

        // Act
        repository.activateTrainee(
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
    void shouldDeactivateTrainee() {
        // Arrange
        Trainee james = trainee(JAMES);
        Long id = james.getId();
        reload();

        // Act
        repository.deactivateTrainee(
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
    void shouldRejectNullTraineeOnActivation() {
        // Arrange
        Trainee invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.activateTrainee(invalid)
        );
    }

    @Test
    void shouldRejectNullTraineeOnDeactivation() {
        // Arrange
        Trainee invalid = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.deactivateTrainee(invalid)
        );
    }

    @Test
    void shouldRejectActivationForUnknownTrainee() {
        // Arrange
        Trainee invalid = Trainee.builder()
                .id(999999L)
                .build();

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> repository.activateTrainee(invalid)
        );
    }

    @Test
    void shouldFindAllTrainingsWithoutFilters() {
        // Arrange
        TrainingType fitness = type("Fitness");
        Trainee james = trainee(JAMES);
        Trainer sara = trainer(SARA, fitness);
        Training session = training(james, sara, fitness);
        Long expectedId = session.getId();
        reload();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, null, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
        assertEquals(expectedId, actual.getFirst().getId());
    }

    @Test
    void shouldFilterTrainingsByFromDate() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        FROM, null, null, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByToDate() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, TO, null, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByDateRange() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        FROM, TO, null, null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldFilterTrainingsByTrainerName() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, "Sara Connor", null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldReturnEmptyForDifferentTrainerName() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, "Jym Kerry", null
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldFilterTrainingsByTrainingType() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, null, "Fitness"
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldReturnEmptyForDifferentTrainingType() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, null, "Yoga"
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldIgnoreBlankOptionalFilters() {
        // Arrange
        createTrainingFixture();

        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, "  ", "  "
                );

        // Act
        List<Training> actual =
                repository.findTrainings(JAMES, criteria);

        // Assert
        assertEquals(1, actual.size());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        // Arrange
        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        TO.plusDays(1),
                        FROM,
                        null,
                        null
                );

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findTrainings(JAMES, criteria)
        );
    }

    @Test
    void shouldRejectNullTrainingCriteria() {
        // Arrange
        TraineeTrainingCriteria criteria = null;

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.findTrainings(JAMES, criteria)
        );
    }

    @Test
    void shouldRejectBlankUsernameForTrainingSearch() {
        // Arrange
        TraineeTrainingCriteria criteria =
                new TraineeTrainingCriteria(
                        null, null, null, null
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
