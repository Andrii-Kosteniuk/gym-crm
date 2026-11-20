package com.training.gym.persistence.repository;

import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserAlreadyExistsException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.TraineeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JpaTraineeRepository implements TraineeRepository {

    public static final String TRAINEE_MUST_NOT_BE_NULL = "Trainee must not be null";
    public static final String NOT_NULL_ID_MESSAGE = "Trainee ID must not be null";
    public static final String USERNAME_MUST_NOT_BE_BLANK = "Trainee username must not be blank";
    public static final String USERNAME = "username";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TraineeEntity save(TraineeEntity trainee) {
        Objects.requireNonNull(trainee);

        if (trainee.getId() != null) {
            throw new UserAlreadyExistsException("Cannot persist a trainee with an existing ID");
        }

        entityManager.persist(trainee);

        return trainee;
    }

    @Override
    public TraineeEntity update(TraineeEntity trainee) {
        Objects.requireNonNull(trainee, "Trainee is required");
        Objects.requireNonNull(trainee.getId(), "Trainee ID is required");
        Objects.requireNonNull(trainee.getUserEntity(), "User is required");

        TraineeEntity traineeEntity = entityManager.find(
                TraineeEntity.class, trainee.getId());

        if (traineeEntity == null) {
            throw new EntityNotFoundException("Trainee not found: " + trainee.getId());
        }

        traineeEntity.getUserEntity().setFirstName(trainee.getUserEntity().getFirstName());

        traineeEntity.getUserEntity().setLastName(trainee.getUserEntity().getLastName());

        return traineeEntity;
    }

    @Override
    public Optional<TraineeEntity> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        return entityManager.createQuery("""
                SELECT t
                FROM TraineeEntity t
                JOIN FETCH t.userEntity
                WHERE t.id = :id
                """, TraineeEntity.class)
                .setParameter("id", id)
                .getResultList().stream()
                .findFirst();
    }

    @Override
    public Optional<TraineeEntity> findByUserName(String username) {
        validateUsername(username);

        return entityManager.createQuery("""
                        SELECT t
                        FROM TraineeEntity t
                        JOIN FETCH t.userEntity u
                        WHERE u.userName = :username
                        """, TraineeEntity.class)
                .setParameter(USERNAME, username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<TraineeEntity> findAll() {
        return entityManager.createQuery("""
                        SELECT t FROM TraineeEntity t
                        JOIN FETCH t.userEntity
                        """, TraineeEntity.class)
                .getResultList()
                .stream()
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        Long count = entityManager.createQuery("""
                SELECT COUNT(t)
                FROM TraineeEntity t
                WHERE t.id = :id
                """, Long.class)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void delete(TraineeEntity trainee) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);

        Long id = Objects.requireNonNull(trainee.getId(), NOT_NULL_ID_MESSAGE);

        TraineeEntity entity = entityManager.find(TraineeEntity.class, id);

        if (entity == null) {
            throw new EntityNotFoundException("Trainee not found: " + id);
        }

        entityManager.remove(entity);
    }

    @Override
    public void deleteByUsername(String username) {
        validateUsername(username);

        TraineeEntity trainee = findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        delete(trainee);
    }

    @Override
    public void activateTrainee(TraineeEntity trainee) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);
        changeActive(trainee.getId(), true);
    }

    @Override
    public void deactivateTrainee(TraineeEntity trainee) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);
        changeActive(trainee.getId(), false);
    }

    @Override
    public void replaceTrainers(Long traineeId, Set<Long> trainerIds) {
        Objects.requireNonNull(traineeId);
        Objects.requireNonNull(trainerIds);

        TraineeEntity trainee = entityManager.find(
                TraineeEntity.class, traineeId);

        if (trainee == null) {
            throw new EntityNotFoundException("Trainee not found: " + traineeId);
        }

        Set<TrainerEntity> trainers = new HashSet<>();

        for (Long trainerId : trainerIds) {
            TrainerEntity trainer = entityManager.find(
                    TrainerEntity.class, trainerId);

            if (trainer == null) {
                throw new EntityNotFoundException("Trainer not found: " + trainerId);
            }

            trainers.add(trainer);
        }

        trainee.getTrainerEntities().clear();
        trainee.getTrainerEntities().addAll(trainers);
    }


    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IncorrectMethodArgumentException(USERNAME_MUST_NOT_BE_BLANK);
        }
    }

    private void changeActive(Long id, boolean active) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        TraineeEntity trainee = entityManager.find(TraineeEntity.class, id);

        if (trainee == null) {
            throw new EntityNotFoundException("Trainee not found: " + id);
        }

        UserEntity user = trainee.getUserEntity();

        if (Boolean.TRUE.equals(user.getActive()) == active) {
            throw new IllegalStateException(
                    active
                            ? "Trainee is already active"
                            : "Trainee is already inactive"
            );
        }

        user.setActive(active);
    }

}