package com.training.gym.persistence.repository;

import com.training.gym.persistence.entity.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class JpaTrainingTypeRepositoryTest {
    @Mock
    EntityManager em;
    @Mock
    TypedQuery<TrainingTypeEntity> query;
    JpaTrainingTypeRepository trainingTypeRepository;

    @BeforeEach
    void setup() {
        trainingTypeRepository = new JpaTrainingTypeRepository();
        ReflectionTestUtils.setField(trainingTypeRepository, "entityManager", em);
    }

    @Test
    void findByIdReturnsEntityWithoutMapping() {
        TrainingTypeEntity trainingTypeEntity = new TrainingTypeEntity();
        trainingTypeEntity.setId(3L);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(trainingTypeEntity);
        assertSame(trainingTypeEntity, trainingTypeRepository.findById(3L).orElseThrow());
        assertTrue(trainingTypeRepository.findById(4L).isEmpty());
        assertThrows(NullPointerException.class, () -> trainingTypeRepository.findById(null));
    }

    @Test
    void findAllReturnsJpaEntities() {
        TrainingTypeEntity trainingTypeEntity = new TrainingTypeEntity();
        when(em.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainingTypeEntity));
        assertEquals(List.of(trainingTypeEntity), trainingTypeRepository.findAll());
    }
}
