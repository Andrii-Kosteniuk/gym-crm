package com.training.gym.persistence.repository;

import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserAlreadyExistsException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class JpaTraineeRepositoryTest {
    @Mock
    EntityManager em;
    @Mock
    TypedQuery<TraineeEntity> query;
    @Mock
    TypedQuery<Long> countQuery;
    JpaTraineeRepository traineeRepository;

    @BeforeEach
    void setup() {
        traineeRepository = new JpaTraineeRepository();
        ReflectionTestUtils.setField(traineeRepository, "entityManager", em);
    }

    private TraineeEntity entity(Long id, boolean active) {
        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(id);
        UserEntity user = new UserEntity();
        user.setFirstName("James");
        user.setLastName("Bond");
        user.setActive(active);
        traineeEntity.setUserEntity(user);
        return traineeEntity;
    }

    @Test
    void savePersistsEntityDirectly() {
        TraineeEntity traineeEntity = entity(null, true);
        assertSame(traineeEntity, traineeRepository.save(traineeEntity));
        verify(em).persist(traineeEntity);
        assertThrows(NullPointerException.class, () -> traineeRepository.save(null));
        assertThrows(UserAlreadyExistsException.class, () -> traineeRepository.save(entity(1L, true)));
    }

    @Test
    void updateModifiesManagedUser() {
        TraineeEntity traineeEntity = entity(1L, true);
        TraineeEntity changesTrainee = entity(1L, true);
        changesTrainee.getUserEntity().setFirstName("Updated");
        when(em.find(TraineeEntity.class, 1L)).thenReturn(traineeEntity);
        assertSame(traineeEntity, traineeRepository.update(changesTrainee));
        assertEquals("Updated", traineeEntity.getUserEntity().getFirstName());
        verify(em, never()).merge(any());
    }

    @Test
    void updateRejectsMissingInvalidEntity() {
        assertThrows(NullPointerException.class, () -> traineeRepository.update(null));
        assertThrows(NullPointerException.class, () -> traineeRepository.update(entity(null, true)));
        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        assertThrows(NullPointerException.class, () -> traineeRepository.update(traineeEntity));
        assertThrows(EntityNotFoundException.class, () -> traineeRepository.update(entity(1L, true)));
    }

    @Test
    void findByIdAndUsernameReturnEntities() {
        TraineeEntity traineeEntity = entity(1L, true);
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("id", 1L)).thenReturn(query);
        when(query.setParameter("username", "james.bond")).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(traineeEntity), List.of(traineeEntity));
        assertSame(traineeEntity, traineeRepository.findById(1L).orElseThrow());
        assertSame(traineeEntity, traineeRepository.findByUserName("james.bond").orElseThrow());
    }

    @Test
    void missingFindAndFindAllAreHandled() {
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("id", 2L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());
        assertTrue(traineeRepository.findById(2L).isEmpty());
        assertTrue(traineeRepository.findAll().isEmpty());
        assertThrows(NullPointerException.class, () -> traineeRepository.findById(null));
        assertThrows(IncorrectMethodArgumentException.class, () -> traineeRepository.findByUserName(" "));
    }

    @Test
    void existsByIdReturnsCount() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 1L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);
        assertTrue(traineeRepository.existsById(1L));
        assertFalse(traineeRepository.existsById(1L));
        assertThrows(NullPointerException.class, () -> traineeRepository.existsById(null));
    }

    @Test
    void deleteRemovesManagedEntityAndRejectsMissing() {
        TraineeEntity traineeEntity = entity(1L, true), managedTrainee = entity(1L, true);
        when(em.find(TraineeEntity.class, 1L)).thenReturn(managedTrainee);
        traineeRepository.delete(traineeEntity);
        verify(em).remove(managedTrainee);
        assertThrows(NullPointerException.class, () -> traineeRepository.delete(null));
        assertThrows(NullPointerException.class, () -> traineeRepository.delete(new TraineeEntity()));
    }

    @Test
    void deleteThrowsIfNotPresent() {
        assertThrows(EntityNotFoundException.class, () -> traineeRepository.delete(entity(1L, true)));
        assertThrows(IncorrectMethodArgumentException.class, () -> traineeRepository.deleteByUsername(" "));
    }

    @Test
    void activationChangesManagedUserStateAndRejectsDuplicate() {
        TraineeEntity activeTrainee = entity(1L, true), inactiveTrainee = entity(1L, false);
        when(em.find(TraineeEntity.class, 1L)).thenReturn(inactiveTrainee, activeTrainee, entity(1L, true), entity(1L, false));
        traineeRepository.activateTrainee(inactiveTrainee);
        assertTrue(inactiveTrainee.getUserEntity().getActive());
        traineeRepository.deactivateTrainee(activeTrainee);
        assertFalse(activeTrainee.getUserEntity().getActive());
        assertThrows(IllegalStateException.class, () -> traineeRepository.activateTrainee(activeTrainee));
        assertThrows(IllegalStateException.class, () -> traineeRepository.deactivateTrainee(inactiveTrainee));
    }

    @Test
    void activationRejectsNullAndMissingEntity() {
        assertThrows(NullPointerException.class, () -> traineeRepository.activateTrainee(null));
        assertThrows(NullPointerException.class, () -> traineeRepository.deactivateTrainee(null));
        assertThrows(EntityNotFoundException.class, () -> traineeRepository.activateTrainee(entity(1L, true)));
    }

    @Test
    void replacingTrainersUsesManagedAssociations() {
        TraineeEntity traineeEntity = entity(1L, true);
        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setId(2L);
        when(em.find(TraineeEntity.class, 1L)).thenReturn(traineeEntity);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(trainerEntity);
        traineeRepository.replaceTrainers(1L, Set.of(2L));
        assertEquals(Set.of(trainerEntity), traineeEntity.getTrainerEntities());
    }

    @Test
    void replacingTrainersRejectsMissingReferences() {
        assertThrows(NullPointerException.class, () -> traineeRepository.replaceTrainers(null, Set.of()));
        assertThrows(NullPointerException.class, () -> traineeRepository.replaceTrainers(1L, null));
        assertThrows(EntityNotFoundException.class, () -> traineeRepository.replaceTrainers(1L, Set.of()));
        when(em.find(TraineeEntity.class, 1L)).thenReturn(entity(1L, true));
        assertThrows(EntityNotFoundException.class, () -> traineeRepository.replaceTrainers(1L, Set.of(2L)));
    }
}
