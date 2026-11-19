package com.training.gym.persistence.repository;

import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.entity.UserEntity;
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
class JpaTrainerRepositoryTest {
    @Mock
    EntityManager em;
    @Mock
    TypedQuery<TrainerEntity> query;
    @Mock
    TypedQuery<Long> countQuery;
    JpaTrainerRepository trainerRepository;

    @BeforeEach
    void setup() {
        trainerRepository = new JpaTrainerRepository();
        ReflectionTestUtils.setField(trainerRepository, "entityManager", em);
    }

    private TrainerEntity entity(Long id, boolean active) {
        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setId(id);
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Sara");
        userEntity.setLastName("Connor");
        userEntity.setActive(active);
        trainerEntity.setUserEntity(userEntity);
        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(3L);
        trainerEntity.setSpecialization(type);
        return trainerEntity;
    }

    @Test
    void savePersistsJpaEntityWithManagedSpecialization() {
        TrainerEntity trainerEntity = entity(null, true);
        TrainingTypeEntity trainingTypeEntity = new TrainingTypeEntity();
        trainingTypeEntity.setId(3L);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(trainingTypeEntity);
        assertSame(trainerEntity, trainerRepository.save(trainerEntity));
        assertSame(trainingTypeEntity, trainerEntity.getSpecialization());
        verify(em).persist(trainerEntity);
    }

    @Test
    void saveRejectsInvalidReferences() {
        assertThrows(NullPointerException.class, () -> trainerRepository.save(null));
        assertThrows(IllegalArgumentException.class, () -> trainerRepository.save(entity(2L, true)));
        assertThrows(EntityNotFoundException.class, () -> trainerRepository.save(entity(null, true)));
    }

    @Test
    void updateModifiesManagedEntity() {
        TrainerEntity trainerEntity = entity(2L, true);
        trainerEntity.getUserEntity().setFirstName("Updated");
        TrainerEntity managedTrainer = entity(2L, true);
        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(3L);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(managedTrainer);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(type);
        assertSame(managedTrainer, trainerRepository.update(trainerEntity));
        assertEquals("Updated", managedTrainer.getUserEntity().getFirstName());
        assertSame(type, managedTrainer.getSpecialization());
        verify(em, never()).merge(any());
    }

    @Test
    void updateRejectsMissingAndInvalidEntity() {
        assertThrows(NullPointerException.class, () -> trainerRepository.update(null));
        assertThrows(NullPointerException.class, () -> trainerRepository.update(entity(null, true)));
        assertThrows(EntityNotFoundException.class, () -> trainerRepository.update(entity(2L, true)));
    }

    @Test
    void findByIdAndUsernameReturnEntities() {
        TrainerEntity trainerEntity = entity(2L, true);
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("id", 2L)).thenReturn(query);
        when(query.setParameter("username", "sara.connor")).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.of(trainerEntity).stream());
        when(query.getResultList()).thenReturn(List.of(trainerEntity));
        assertSame(trainerEntity, trainerRepository.findById(2L).orElseThrow());
        assertSame(trainerEntity, trainerRepository.findByUserName("sara.connor").orElseThrow());
    }

    @Test
    void missingFindAndFindAllAreHandled() {
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("id", 2L)).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.<TrainerEntity>of().stream());
        when(query.getResultList()).thenReturn(List.of());
        assertTrue(trainerRepository.findById(2L).isEmpty());
        assertTrue(trainerRepository.findAll().isEmpty());
        assertThrows(NullPointerException.class, () -> trainerRepository.findById(null));
        assertThrows(IllegalArgumentException.class, () -> trainerRepository.findByUserName(" "));
    }

    @Test
    void existsByIdReturnsCount() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 2L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);
        assertTrue(trainerRepository.existsById(2L));
        assertFalse(trainerRepository.existsById(2L));
        assertThrows(NullPointerException.class, () -> trainerRepository.existsById(null));
    }

    @Test
    void deleteRemovesManagedEntity() {
        TrainerEntity trainerEntity = entity(2L, true), managedTrainer = entity(2L, true);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(managedTrainer);
        trainerRepository.delete(trainerEntity);
        verify(em).remove(managedTrainer);
    }

    @Test
    void deleteRejectsInvalidOrMissing() {
        assertThrows(NullPointerException.class, () -> trainerRepository.delete(null));
        assertThrows(NullPointerException.class, () -> trainerRepository.delete(entity(null, true)));
        assertThrows(EntityNotFoundException.class, () -> trainerRepository.delete(entity(2L, true)));
        assertThrows(IllegalArgumentException.class, () -> trainerRepository.deleteByUsername(""));
    }

    @Test
    void activationUsesManagedUserAndRejectsDuplicates() {
        TrainerEntity inactiveTrainer = entity(2L, false), activeTrainer = entity(2L, true);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(inactiveTrainer, activeTrainer, entity(2L, true), entity(2L, false));
        trainerRepository.activateTrainer(inactiveTrainer);
        assertTrue(inactiveTrainer.getUserEntity().getActive());
        trainerRepository.deactivateTrainer(activeTrainer);
        assertFalse(activeTrainer.getUserEntity().getActive());
        assertThrows(IllegalStateException.class, () -> trainerRepository.activateTrainer(activeTrainer));
        assertThrows(IllegalStateException.class, () -> trainerRepository.deactivateTrainer(inactiveTrainer));
    }

    @Test
    void activationRejectsNullAndMissing() {
        assertThrows(NullPointerException.class, () -> trainerRepository.activateTrainer(null));
        assertThrows(NullPointerException.class, () -> trainerRepository.deactivateTrainer(null));
        assertThrows(EntityNotFoundException.class, () -> trainerRepository.activateTrainer(entity(2L, true)));
    }

    @Test
    void unassignedQueryRejectsUnknownTrainee() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", "missing")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        assertThrows(EntityNotFoundException.class, () -> trainerRepository.findUnassignedTrainers("missing"));
        assertThrows(IllegalArgumentException.class, () -> trainerRepository.findUnassignedTrainers(" "));
    }

    @Test
    void unassignedQueryReturnsEntities() {
        TrainerEntity trainerEntity = entity(2L, true);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", "james.bond")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("username", "james.bond")).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.of(trainerEntity).stream());
        assertEquals(List.of(trainerEntity), trainerRepository.findUnassignedTrainers("james.bond"));
    }
}
