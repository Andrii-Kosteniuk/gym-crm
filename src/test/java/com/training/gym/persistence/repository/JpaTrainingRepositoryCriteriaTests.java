
package com.training.gym.persistence.repository;

import com.training.gym.config.PersistenceConfig;
import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.persistence.entity.TrainingEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(PersistenceConfig.class)
@ActiveProfiles("test")
@Transactional
class JpaTrainingRepositoryCriteriaTests {

    private static final String TRAINEE_USERNAME = "james.bond";
    private static final String OTHER_TRAINEE_USERNAME = "jane.wick";
    private static final String TRAINER_USERNAME = "sarah.connor";
    private static final String OTHER_TRAINER_USERNAME = "john.Djon";

    private static final String FITNESS = "Fitness";
    private static final String YOGA = "Yoga";

    private static final LocalDate JAN_10 = LocalDate.of(2026, 1, 10);
    private static final LocalDate JAN_20 = LocalDate.of(2026, 1, 20);
    private static final LocalDate FEB_10 = LocalDate.of(2026, 2, 10);

    @Autowired
    private JpaTrainingRepository repository;

    @PersistenceContext
    private EntityManager em;

    private Long firstId;
    private Long secondId;
    private Long thirdId;

    @BeforeEach
    void setUp() {
        assertTrainingTypeExists(FITNESS);
        assertTrainingTypeExists(YOGA);

        insertUser("James", "Bond", TRAINEE_USERNAME);
        insertUser("Jane", "Wick", OTHER_TRAINEE_USERNAME);
        insertUser("Sara", "Connor", TRAINER_USERNAME);
        insertUser("John", "Djon", OTHER_TRAINER_USERNAME);

        insertTrainee(TRAINEE_USERNAME);
        insertTrainee(OTHER_TRAINEE_USERNAME);

        insertTrainer(TRAINER_USERNAME);
        insertTrainer(OTHER_TRAINER_USERNAME);

        firstId = insertTraining(
                TRAINEE_USERNAME, TRAINER_USERNAME,
                FITNESS, "Morning workout", JAN_10);

        secondId = insertTraining(
                TRAINEE_USERNAME, OTHER_TRAINER_USERNAME,
                YOGA, "Evening yoga", JAN_20);

        thirdId = insertTraining(
                OTHER_TRAINEE_USERNAME, TRAINER_USERNAME,
                FITNESS, "Strength", FEB_10);

        em.flush();
        em.clear();
    }

    private void insertUser(String firstName, String lastName, String username) {
        em.createNativeQuery("""
                        INSERT INTO users
                            (first_name, last_name, username, password, active)
                        VALUES (:firstName, :lastName, :username, 'hash', true)
                        """)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("username", username)
                .executeUpdate();
    }

    private void insertTrainee(String username) {
        em.createNativeQuery("""
                        INSERT INTO trainees (id)
                        SELECT id FROM users WHERE username = :username
                        """)
                .setParameter("username", username)
                .executeUpdate();
    }

    private void insertTrainer(String username) {
        em.createNativeQuery("""
                        INSERT INTO trainers (id, specialization_id)
                        SELECT u.id, t.id
                        FROM users u, training_types t
                        WHERE u.username = :username AND t.name = :type
                        """)
                .setParameter("username", username)
                .setParameter("type", FITNESS)
                .executeUpdate();
    }

    private Long insertTraining(
            String trainee,
            String trainer,
            String type,
            String name,
            LocalDate date) {

        em.createNativeQuery("""
                        INSERT INTO trainings
                            (trainee_id, trainer_id, training_type_id,
                             name, training_date, duration_minutes)
                        SELECT trainee.id, trainer.id, type.id,
                               :name, :date, 60
                        FROM users trainee, users trainer, training_types type
                        WHERE trainee.username = :trainee
                          AND trainer.username = :trainer
                          AND type.name = :type
                        """)
                .setParameter("name", name)
                .setParameter("date", date)
                .setParameter("trainee", trainee)
                .setParameter("trainer", trainer)
                .setParameter("type", type)
                .executeUpdate();

        return ((Number) em.createNativeQuery(
                        "SELECT MAX(id) FROM trainings")
                .getSingleResult()).longValue();
    }

    private List<Long> ids(List<TrainingEntity> trainings) {
        return trainings.stream()
                .map(TrainingEntity::getId)
                .toList();
    }


    @Test
    void shouldFindAllTraineeTrainings() {
        // Arrange
        var filter = new TraineeTrainingFilter(null, null, null, null);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(secondId, firstId), ids(result));
    }

    @Test
    void shouldFilterTraineeTrainingsByFromDate() {
        // Arrange
        var filter = new TraineeTrainingFilter(JAN_20, null, null, null);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(secondId), ids(result));
    }

    @Test
    void shouldFilterTraineeTrainingsByToDate() {
        // Arrange
        var filter = new TraineeTrainingFilter(null, JAN_10, null, null);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(firstId), ids(result));
    }

    @Test
    void shouldFilterTraineeTrainingsByTrainerName() {
        // Arrange
        var filter = new TraineeTrainingFilter(
                null, null, "Sara Connor", null);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(firstId), ids(result));
    }

    @Test
    void shouldFilterTraineeTrainingsByType() {
        // Arrange
        var filter = new TraineeTrainingFilter(null, null, null, YOGA);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(secondId), ids(result));
    }

    @Test
    void shouldApplyAllTraineeFilters() {
        // Arrange
        var filter = new TraineeTrainingFilter(
                JAN_10, JAN_20, "Sara Connor", FITNESS);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(firstId), ids(result));
    }

    @Test
    void shouldReturnEmptyForUnknownTrainee() {
        // Arrange
        var filter = new TraineeTrainingFilter(null, null, null, null);

        // Act
        var result = repository.findByTraineeUsername("unknown", filter);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenTraineeFiltersDoNotMatch() {
        // Arrange
        var filter = new TraineeTrainingFilter(
                null, null, "Unknown Trainer", null);

        // Act
        var result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldFindAllTrainerTrainings() {
        // Arrange
        var filter = new TrainerTrainingFilter(null, null, null);

        // Act
        var result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(thirdId, firstId), ids(result));
    }

    @Test
    void shouldFilterTrainerTrainingsByFromDate() {
        // Arrange
        var filter = new TrainerTrainingFilter(FEB_10, null, null);

        // Act
        var result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(thirdId), ids(result));
    }

    @Test
    void shouldFilterTrainerTrainingsByToDate() {
        // Arrange
        var filter = new TrainerTrainingFilter(null, JAN_10, null);

        // Act
        var result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(firstId), ids(result));
    }

    @Test
    void shouldFilterTrainerTrainingsByTraineeName() {
        // Arrange
        var filter = new TrainerTrainingFilter(null, null, "James Bond");

        // Act
        var result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(firstId), ids(result));
    }

    @Test
    void shouldApplyAllTrainerFilters() {
        // Arrange
        var filter = new TrainerTrainingFilter(
                JAN_10, JAN_20, "James Bond");

        // Act
        var result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(firstId), ids(result));
    }

    @Test
    void shouldReturnEmptyForUnknownTrainer() {
        // Arrange
        var filter = new TrainerTrainingFilter(null, null, null);

        // Act
        var result = repository.findByTrainerUsername("unknown", filter);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenTrainerFiltersDoNotMatch() {
        // Arrange
        var filter = new TrainerTrainingFilter(
                null, null, "Unknown Trainee");

        // Act
        var result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullTraineeFilter() {
        // Arrange
        String username = TRAINEE_USERNAME;

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> repository.findByTraineeUsername(username, null));
    }

    @Test
    void shouldRejectNullTrainerFilter() {
        // Arrange
        String username = TRAINER_USERNAME;

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> repository.findByTrainerUsername(username, null));
    }

    @Test
    void shouldRejectBlankTraineeUsername() {
        // Arrange
        var filter = new TraineeTrainingFilter(null, null, null, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByTraineeUsername(" ", filter));
    }

    @Test
    void shouldRejectBlankTrainerUsername() {
        // Arrange
        var filter = new TrainerTrainingFilter(null, null, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByTrainerUsername(" ", filter));
    }

    private void assertTrainingTypeExists(String name) {
        Number count = (Number) em.createNativeQuery("""
            SELECT COUNT(*)
            FROM training_types
            WHERE name = :name
            """)
                .setParameter("name", name)
                .getSingleResult();

        assertEquals(1L, count.longValue(),
                "Missing training type: " + name);
    }

}