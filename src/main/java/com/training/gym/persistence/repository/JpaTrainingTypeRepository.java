package com.training.gym.persistence.repository;

import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.mapper.TrainingTypePersistenceMapper;
import com.training.gym.domain.TrainingType;
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

    private final TrainingTypePersistenceMapper trainingTypeMapper;

    @Override
    public Optional<TrainingType> findById(Long id) {
        Objects.requireNonNull(id, NOT_NULL_ID_MESSAGE);

        return Optional.ofNullable(entityManager.find(TrainingTypeEntity.class, id))
                .map(trainingTypeMapper::toTrainingDomain);
    }

    @Override
    public List<TrainingType> findAll() {
        return entityManager.createQuery("""
                SELECT t
                FROM TrainingTypeEntity t
                ORDER BY t.name
                """, TrainingTypeEntity.class)
                .getResultList()
                .stream()
                .map(trainingTypeMapper::toTrainingDomain)
                .toList();
    }
}
