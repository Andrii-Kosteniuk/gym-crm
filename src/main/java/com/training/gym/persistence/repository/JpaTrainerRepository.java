package com.training.gym.persistence.repository;

import com.training.gym.domain.Trainer;
import com.training.gym.domain.TrainingType;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.persistence.mapper.TrainerPersistenceMapper;
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

    private final TrainerPersistenceMapper trainerMapper;

    @Override
    public Trainer save(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        if (trainer.getId() != null) {
            throw new IllegalArgumentException("Cannot persist a Trainer with an existing ID");
        }

        TrainerEntity entity = trainerMapper.toEntity(trainer);

        TrainingTypeEntity specialization = entityManager.find(
                TrainingTypeEntity.class,
                Objects.requireNonNull(trainer.getSpecialization().getId(), "Specialization ID must not be null"));

        if (specialization == null) {
            throw new EntityNotFoundException("Training type not found");
        }

        entity.setSpecialization(specialization);

        entityManager.persist(entity);

        return trainerMapper.toTrainerDomain(entity);
    }

    @Override
    public Trainer update(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        Long id = Objects.requireNonNull(trainer.getId(), NOT_NULL_ID_MESSAGE);

        TrainerEntity entity = entityManager.find(TrainerEntity.class, id);

        if (entity == null) {
            throw new EntityNotFoundException("Trainer not found: " + id);
        }

        entity.getUserEntity().setFirstName(trainer.getUser().getFirstName());

        entity.getUserEntity().setLastName(trainer.getUser().getLastName());


        TrainingType specialization = trainer.getSpecialization();

        TrainingTypeEntity managedType = entityManager.find(TrainingTypeEntity.class, specialization.getId());

        if (managedType == null) {
            throw new EntityNotFoundException("Training type not found");
        }

        entity.setSpecialization(managedType);

        return trainerMapper.toTrainerDomain(entity);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity
                        JOIN FETCH t.specialization
                        WHERE t.id = :id
                        """, TrainerEntity.class)
                .setParameter("id", id
                ).getResultStream()
                .map(trainerMapper::toTrainerDomain)
                .findFirst();
    }

    @Override
    public Optional<Trainer> findByUserName(String username) {
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
                .map(trainerMapper::toTrainerDomain)
                .findFirst();
    }

    @Override
    public void delete(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);

        Long id = Objects.requireNonNull(trainer.getId(), NOT_NULL_ID_MESSAGE);

        TrainerEntity entity = entityManager.find(TrainerEntity.class, id);

        if (entity == null) {
            throw new EntityNotFoundException("Trainer not found: " + id);
        }

        entityManager.remove(entity);
    }

    @Override
    public void deleteByUsername(String username) {
        validateUsername(username);

        Trainer trainer = findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        delete(trainer);
    }

    @Override
    public void activateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);
        changeActive(trainer.getId(), true);
    }

    @Override
    public void deactivateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, TRAINER_MUST_NOT_BE_NULL);
        changeActive(trainer.getId(), false);
    }

    @Override
    public List<Trainer> findAll() {
        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity
                        ORDER BY t.id
                        """, TrainerEntity.class).getResultList()
                .stream()
                .map(trainerMapper::toTrainerDomain)
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
    public List<Trainer> findUnassignedTrainers(String username) {
        validateUsername(username);

        boolean exists = entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM TraineeEntity t
                        WHERE t.userEntity.userName = :username
                        """, Long.class)
                .setParameter(USERNAME, username)

                .getSingleResult() > 0;

        if (!exists) {
            throw new EntityNotFoundException(
                    "Trainee not found: " + username);
        }

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainerEntity t
                        JOIN FETCH t.userEntity
                        WHERE NOT EXISTS (
                        SELECT 1
                        FROM TraineeEntity trainee
                        JOIN trainee.trainerEntities assigned
                        WHERE trainee.userEntity.userName = :username
                        AND assigned = t)
                        ORDER BY t.userEntity.lastName,
                        t.userEntity.firstName
                        """, TrainerEntity.class)
                .setParameter(USERNAME, username)
                .getResultStream()
                .map(trainerMapper::toTrainerDomain)
                .toList();
    }


    private void validateUsername(String username) {
        if (!hasText(username)) {
            throw new IllegalArgumentException(NOT_BLANK_USERNAME_MESSAGE);
        }
    }


    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void changeActive(Long id, boolean active) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        TrainerEntity trainer = entityManager.find(TrainerEntity.class, id);

        if (trainer == null) {
            throw new EntityNotFoundException("Trainer not found: " + id);
        }

        UserEntity user = trainer.getUserEntity();

        if (Boolean.TRUE.equals(user.getActive()) == active) {
            throw new IllegalStateException(
                    active
                            ? "Trainer is already active"
                            : "Trainer is already inactive"
            );
        }

        user.setActive(active);

    }

}