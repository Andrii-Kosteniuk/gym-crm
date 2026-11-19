package com.training.gym.persistence.repository;

import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.repository.TrainingTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTrainingTypeRepository implements TrainingTypeRepository {
    private static final String NOT_NULL_ID_MESSAGE = "TrainingType ID must not be null";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingTypeEntity> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        TrainingTypeEntity trainingType = entityManager.find(TrainingTypeEntity.class, id);

        return Optional.ofNullable(trainingType);
    }

    @Override
    public List<TrainingTypeEntity> findAll() {

        return entityManager.createQuery("""
                        SELECT t
                        FROM TrainingTypeEntity t
                        ORDER BY t.name
                        """, TrainingTypeEntity.class)
                .getResultList()
                .stream()
                .toList();
    }
}
