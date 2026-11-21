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
    public static final String TRAINEE_NOT_FOUND = "Trainee not found: ";

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
            throw new EntityNotFoundException(TRAINEE_NOT_FOUND + trainee.getId());
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
            throw new EntityNotFoundException(TRAINEE_NOT_FOUND + id);
        }

        entityManager.remove(entity);
    }

    @Override
    public void deleteByUsername(String username) {
        validateUsername(username);

        TraineeEntity trainee = findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));

        delete(trainee);
    }

    @Override
    public void changeProfileStatus(TraineeEntity trainee, boolean active) {
        Objects.requireNonNull(trainee, TRAINEE_MUST_NOT_BE_NULL);

        TraineeEntity traineeEntity = entityManager.find(TraineeEntity.class, trainee.getId());

        if (traineeEntity == null) {
            throw new EntityNotFoundException("Trainee not found:");
        }

        UserEntity user = traineeEntity.getUserEntity();

        user.setActive(active);
    }

    @Override
    public void replaceTrainers(String username, Set<Long> trainerIds) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(trainerIds);

        TraineeEntity trainee = findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));

        Set<TrainerEntity> trainers = new HashSet<>();

        for (Long trainerId : trainerIds) {
            TrainerEntity trainer = entityManager.find(
                    TrainerEntity.class, trainerId);

            if (trainer == null) {
                throw new EntityNotFoundException(TRAINEE_NOT_FOUND + trainerId);
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

}