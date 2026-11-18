package com.training.gym.persistence.repository;

import com.training.gym.persistence.entity.*;
import com.training.gym.persistence.mapper.TrainingPersistenceMapper;
import com.training.gym.domain.Training;
import com.training.gym.repository.TrainingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTrainingRepository implements TrainingRepository {

    public static final String TRAINING_MUST_NOT_BE_NULL = "Training must not be null";
    public static final String ID_MUST_NOT_BE_NULL = "Training ID must not be null";

    @PersistenceContext
    private EntityManager entityManager;

    private final TrainingPersistenceMapper trainingMapper;

    @Override
    public Training save(Training training) {
        Objects.requireNonNull(training, TRAINING_MUST_NOT_BE_NULL);

        if (training.getId() != null) {
            throw new IllegalArgumentException("New training must not have an ID");
        }

        TrainingEntity entity = trainingMapper.toTrainingEntity(training);

        Long traineeId = Objects.requireNonNull(training.getTrainee().getId(), "Trainee ID must not be null");

        Long trainerId = Objects.requireNonNull(training.getTrainer().getId(), "Trainer ID must not be null");

        Long typeId = Objects.requireNonNull(training.getTrainingType().getId(), "Training type ID must not be null");

        TraineeEntity traineeEntity = entityManager.find(TraineeEntity.class, traineeId);

        TrainerEntity trainerEntity = entityManager.find(TrainerEntity.class, trainerId);

        TrainingTypeEntity typeEntity = entityManager.find(TrainingTypeEntity.class, typeId);

        if (traineeEntity == null
                || trainerEntity == null
                || typeEntity == null) {
            throw new EntityNotFoundException("Referenced training entity not found");
        }

        entity.setTraineeEntity(traineeEntity);
        entity.setTrainerEntity(trainerEntity);
        entity.setTrainingTypeEntity(typeEntity);

        entityManager.persist(entity);

        training.setId(entity.getId());

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainingEntity t
                        JOIN FETCH t.traineeEntity
                        JOIN FETCH t.trainerEntity
                        JOIN FETCH t.trainingTypeEntity
                        WHERE t.id = :id
                        """, TrainingEntity.class).setParameter("id", id)
                .getResultList()
                .stream()
                .map(trainingMapper::toTrainingDomain)
                .findFirst();
    }

    @Override
    public List<Training> findAll() {
        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainingEntity t
                        ORDER BY t.trainingDate DESC, t.id DESC
                        """, TrainingEntity.class).getResultList()
                .stream()
                .map(trainingMapper::toTrainingDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);

        Long count = entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM TrainingEntity t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Training> findByTraineeUsername(String username, TraineeTrainingFilter filter) {
        validateUsername(username);

        Objects.requireNonNull(filter, "Filter must not be null");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TrainingEntity> cq =
                cb.createQuery(TrainingEntity.class);

        Root<TrainingEntity> training =
                cq.from(TrainingEntity.class);

        Join<TrainingEntity, TraineeEntity> trainee =
                training.join("traineeEntity");

        Join<TraineeEntity, UserEntity> traineeUser =
                trainee.join("userEntity");

        Join<TrainingEntity, TrainerEntity> trainer =
                training.join("trainerEntity");

        Join<TrainerEntity, UserEntity> trainerUser =
                trainer.join("userEntity");

        Join<TrainingEntity, TrainingTypeEntity> type =
                training.join("trainingTypeEntity");

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                cb.equal(
                        traineeUser.get("userName"),
                        username
                )
        );

        if (filter.fromDate() != null) {
            predicates.add(
                    cb.greaterThanOrEqualTo(
                            training.get("trainingDate"),
                            filter.fromDate()
                    )
            );
        }

        if (filter.toDate() != null) {
            predicates.add(
                    cb.lessThanOrEqualTo(
                            training.get("trainingDate"),
                            filter.toDate()
                    )
            );
        }

        if (filter.trainerName() != null) {
            predicates.add(
                    cb.equal(
                            cb.concat(
                                    cb.concat(
                                            trainerUser.get("firstName"),
                                            " "
                                    ),
                                    trainerUser.get("lastName")
                            ),
                            filter.trainerName()
                    )
            );
        }

        if (filter.trainingType() != null) {
            predicates.add(
                    cb.equal(
                            type.get("name"),
                            filter.trainingType()
                    )
            );
        }

        cq.select(training)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(
                        cb.desc(training.get("trainingDate")),
                        cb.desc(training.get("id"))
                );

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(trainingMapper::toTrainingDomain)
                .toList();
    }

    @Override
    public List<Training> findByTrainerUsername(String username, TrainerTrainingFilter filter) {
        validateUsername(username);
        Objects.requireNonNull(filter, "Filter must not be null");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<TrainingEntity> cq =
                cb.createQuery(TrainingEntity.class);

        Root<TrainingEntity> training =
                cq.from(TrainingEntity.class);

        Join<TrainingEntity, TrainerEntity> trainer =
                training.join("trainerEntity");

        Join<TrainerEntity, UserEntity> trainerUser =
                trainer.join("userEntity");

        Join<TrainingEntity, TraineeEntity> trainee =
                training.join("traineeEntity");

        Join<TraineeEntity, UserEntity> traineeUser =
                trainee.join("userEntity");

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                cb.equal(
                        trainerUser.get("userName"),
                        username
                )
        );

        if (filter.fromDate() != null) {
            predicates.add(
                    cb.greaterThanOrEqualTo(
                            training.get("trainingDate"),
                            filter.fromDate()
                    )
            );
        }

        if (filter.toDate() != null) {
            predicates.add(
                    cb.lessThanOrEqualTo(
                            training.get("trainingDate"),
                            filter.toDate()
                    )
            );
        }

        if (filter.traineeName() != null) {
            predicates.add(
                    cb.equal(
                            cb.concat(
                                    cb.concat(
                                            traineeUser.get("firstName"),
                                            " "
                                    ),
                                    traineeUser.get("lastName")
                            ),
                            filter.traineeName()
                    )
            );
        }

        cq.select(training)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(
                        cb.desc(training.get("trainingDate")),
                        cb.desc(training.get("id"))
                );

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(trainingMapper::toTrainingDomain)
                .toList();
    }


    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
    }
}