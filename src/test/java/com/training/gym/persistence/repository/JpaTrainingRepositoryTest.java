package com.training.gym.persistence.repository;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
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
class JpaTrainingRepositoryTest {
    @Mock
    EntityManager em;
    @Mock
    TypedQuery<TrainingEntity> query;
    @Mock
    TypedQuery<Long> countQuery;
    JpaTrainingRepository trainingRepository;

    @BeforeEach
    void setup() {
        trainingRepository = new JpaTrainingRepository();
        ReflectionTestUtils.setField(trainingRepository, "entityManager", em);
    }

    private TrainingEntity training(Long id) {
        TrainingEntity trainingEntity = new TrainingEntity();
        trainingEntity.setId(id);
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        trainingEntity.setTraineeEntity(trainee);
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(2L);
        trainingEntity.setTrainerEntity(trainer);
        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(3L);
        trainingEntity.setTrainingTypeEntity(type);
        return trainingEntity;
    }

    @Test
    void saveResolvesManagedReferencesAndPersistsJpaEntity() {
        TrainingEntity trainingEntity = training(null);
        TraineeEntity traineeEntity = new TraineeEntity();
        TrainerEntity trainerEntity = new TrainerEntity();
        TrainingTypeEntity trainingTypeEntity = new TrainingTypeEntity();

        when(em.find(TraineeEntity.class, 1L)).thenReturn(traineeEntity);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(trainerEntity);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(trainingTypeEntity);
        assertSame(trainingEntity, trainingRepository.save(trainingEntity));
        assertSame(traineeEntity, trainingEntity.getTraineeEntity());
        assertSame(trainerEntity, trainingEntity.getTrainerEntity());
        assertSame(trainingTypeEntity, trainingEntity.getTrainingTypeEntity());
        verify(em).persist(trainingEntity);
    }

    @Test
    void saveRejectsMissingAndInvalidReferences() {
        assertThrows(NullPointerException.class, () -> trainingRepository.save(null));
        assertThrows(IllegalArgumentException.class, () -> trainingRepository.save(training(10L)));
        TrainingEntity trainingEntity = training(null);
        trainingEntity.setTraineeEntity(new TraineeEntity());
        assertThrows(NullPointerException.class, () -> trainingRepository.save(trainingEntity));
        assertThrows(EntityNotFoundException.class, () -> trainingRepository.save(training(null)));
        verify(em, never()).persist(any());
    }

    @Test
    void findByIdReturnsJpaEntity() {
        TrainingEntity trainingEntity = training(10L);
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.setParameter("id", 10L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainingEntity));
        assertSame(trainingEntity, trainingRepository.findById(10L).orElseThrow());
        assertThrows(NullPointerException.class, () -> trainingRepository.findById(null));
    }

    @Test
    void findAllReturnsJpaEntities() {
        TrainingEntity trainingEntity = training(10L);
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainingEntity));
        assertEquals(List.of(trainingEntity), trainingRepository.findAll());
    }

    @Test
    void existsByIdUsesCountQuery() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 10L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);
        assertTrue(trainingRepository.existsById(10L));
        assertFalse(trainingRepository.existsById(10L));
        assertThrows(NullPointerException.class, () -> trainingRepository.existsById(null));
    }

    @Test
    void searchValidatesUsernameAndFilterBeforeCriteriaQuery() {
        TraineeTrainingFilter traineeFilter = new TraineeTrainingFilter(null, null, null, null);
        TrainerTrainingFilter trainerFilter = new TrainerTrainingFilter(null, null, null);
        assertThrows(IllegalArgumentException.class, () -> trainingRepository.findByTraineeUsername(" ", traineeFilter));
        assertThrows(IllegalArgumentException.class, () -> trainingRepository.findByTrainerUsername(null, trainerFilter));
        assertThrows(NullPointerException.class, () -> trainingRepository.findByTraineeUsername("james.bond", null));
        assertThrows(NullPointerException.class, () -> trainingRepository.findByTrainerUsername("sara.connor", null));
        verifyNoInteractions(em);
    }
}
