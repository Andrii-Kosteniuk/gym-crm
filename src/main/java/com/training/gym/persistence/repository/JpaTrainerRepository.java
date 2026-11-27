package com.training.gym.persistence.repository;

import com.training.gym.exception.UserNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserAlreadyExistsException;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.TrainerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTrainerRepository implements TrainerRepository {

    private static final String NOT_NULL_ID_MESSAGE = "Trainer ID must not be null";
    public static final String TRAINER_MUST_NOT_BE_NULL = "Trainer must not be null";
    private static final String NOT_BLANK_USERNAME_MESSAGE = "Trainer username must not be blank";
    public static final String USERNAME = "username";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TrainerEntity save(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        if (trainer.getId() != null) {
            throw new UserAlreadyExistsException("Cannot persist a trainer with an existing ID");
        }


        TrainingTypeEntity specialization = entityManager.find(
                TrainingTypeEntity.class,
                Objects.requireNonNull(trainer.getSpecialization().getId(), "Specialization ID must not be null"));

        if (specialization == null) {
            throw new UserNotFoundException("Training type not found");
        }

        trainer.setSpecialization(specialization);

        entityManager.persist(trainer);

        return trainer;
    }

    @Override
    public TrainerEntity update(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        Long id = Objects.requireNonNull(trainer.getId(), NOT_NULL_ID_MESSAGE);

        TrainerEntity entity = entityManager.find(TrainerEntity.class, id);

        if (entity == null) {
            throw new UserNotFoundException("Trainer not found: " + id);
        }

        entity.getUserEntity().setFirstName(trainer.getUserEntity().getFirstName());

        entity.getUserEntity().setLastName(trainer.getUserEntity().getLastName());


        TrainingTypeEntity specialization = trainer.getSpecialization();

        TrainingTypeEntity managedType = entityManager.find(TrainingTypeEntity.class, specialization.getId());

        if (managedType == null) {
            throw new UserNotFoundException("Training type not found");
        }

        entity.setSpecialization(managedType);

        return entity;
    }

    @Override
    public Optional<TrainerEntity> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity
                        JOIN FETCH t.specialization
                        WHERE t.id = :id
                        """, TrainerEntity.class)
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<TrainerEntity> findByUserName(String username) {
        validateUsername(username);

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity
                        JOIN FETCH t.specialization
                        WHERE t.userEntity.userName = :username
                        """, TrainerEntity.class)
                .setParameter(USERNAME, username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void delete(TrainerEntity trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        Long id = Objects.requireNonNull(trainer.getId(), NOT_NULL_ID_MESSAGE);

        TrainerEntity entity = entityManager.find(TrainerEntity.class, id);

        if (entity == null) {
            throw new UserNotFoundException("Trainer not found: " + id);
        }

        entityManager.remove(entity);
    }

    @Override
    public void deleteByUsername(String username) {
        validateUsername(username);

        TrainerEntity trainer = findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("Trainer not found: " + username));

        delete(trainer);
    }

    @Override
    public void changeProfileStatus(TrainerEntity trainer, boolean active) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        TrainerEntity trainerEntity = entityManager.find(TrainerEntity.class, trainer.getId());

        if (trainerEntity == null) {
            throw new UserNotFoundException("Trainer not found:");
        }

        UserEntity user = trainerEntity.getUserEntity();

        user.setActive(active);
    }

    @Override
    public List<TrainerEntity> findAll() {
        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity
                        ORDER BY t.id
                        """, TrainerEntity.class).getResultList()
                .stream()
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        Long count = entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM TrainerEntity t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<TrainerEntity> findUnassignedTrainers(String username) {
        validateUsername(username);

        boolean exists = entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM TraineeEntity t
                        WHERE t.userEntity.userName = :username
                        """, Long.class)
                .setParameter(USERNAME, username)

                .getSingleResult() > 0;

        if (!exists) {
            throw new UserNotFoundException("Trainee not found: " + username);
        }

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity u
                        WHERE u.active = true
                          AND NOT EXISTS (
                              SELECT 1
                              FROM TraineeEntity trainee
                              JOIN trainee.trainerEntities assigned
                              WHERE trainee.userEntity.userName = :username
                                AND assigned = t)
                        ORDER BY u.lastName, u.firstName
                        """, TrainerEntity.class)
                .setParameter(USERNAME, username)
                .getResultList();
    }


    private void validateUsername(String username) {
        if (!hasText(username)) {
            throw new IncorrectMethodArgumentException(NOT_BLANK_USERNAME_MESSAGE);
        }
    }


    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}