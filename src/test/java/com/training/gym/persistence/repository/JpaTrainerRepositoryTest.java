package com.training.gym.persistence.repository;

import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserAlreadyExistsException;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTrainerRepositoryTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<TrainerEntity> query;

    @Mock
    TypedQuery<Long> countQuery;

    JpaTrainerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaTrainerRepository();
        ReflectionTestUtils.setField(repository, "entityManager", em);
    }

    private TrainerEntity trainer(Long id, boolean active) {
        UserEntity user = new UserEntity();
        user.setFirstName("Sara");
        user.setLastName("Connor");
        user.setActive(active);

        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(3L);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(id);
        trainer.setUserEntity(user);
        trainer.setSpecialization(type);
        return trainer;
    }

    @Test
    void savePersistsTrainerWithManagedSpecialization() {
        TrainerEntity trainer = trainer(null, true);
        TrainingTypeEntity managedType = new TrainingTypeEntity();
        managedType.setId(3L);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(managedType);

        TrainerEntity result = repository.save(trainer);

        assertSame(trainer, result);
        assertSame(managedType, trainer.getSpecialization());
        verify(em).persist(trainer);
    }

    @Test
    void saveRejectsNullTrainer() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        verifyNoInteractions(em);
    }

    @Test
    void saveRejectsExistingTrainer() {
        TrainerEntity trainer = trainer(2L, true);

        assertThrows(UserAlreadyExistsException.class, () -> repository.save(trainer));

        verify(em, never()).persist(any());
    }

    @Test
    void saveRejectsUnknownSpecialization() {
        TrainerEntity trainer = trainer(null, true);

        assertThrows(EntityNotFoundException.class, () -> repository.save(trainer));

        verify(em, never()).persist(any());
    }

    @Test
    void updateChangesManagedTrainer() {
        TrainerEntity changes = trainer(2L, true);
        changes.getUserEntity().setFirstName("Updated");
        TrainerEntity managed = trainer(2L, true);
        TrainingTypeEntity managedType = new TrainingTypeEntity();
        managedType.setId(3L);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(managed);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(managedType);

        TrainerEntity result = repository.update(changes);

        assertSame(managed, result);
        assertEquals("Updated", managed.getUserEntity().getFirstName());
        assertSame(managedType, managed.getSpecialization());
        verify(em, never()).merge(any());
    }

    @Test
    void updateRejectsInvalidTrainer() {
        assertThrows(NullPointerException.class, () -> repository.update(null));
        assertThrows(NullPointerException.class, () -> repository.update(trainer(null, true)));
    }

    @Test
    void updateRejectsMissingTrainer() {
        TrainerEntity trainer = trainer(2L, true);

        assertThrows(EntityNotFoundException.class, () -> repository.update(trainer));

        verify(em, never()).merge(any());
    }

    @Test
    void findByIdReturnsTrainer() {
        TrainerEntity trainer = trainer(2L, true);
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("id", 2L)).thenReturn(query);
        when(query.getResultList()).thenAnswer(invocation -> List.of(trainer));

        var result = repository.findById(2L);

        assertTrue(result.isPresent());
        assertSame(trainer, result.orElseThrow());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("id", 2L)).thenReturn(query);
        when(query.getResultList()).thenAnswer(invocation -> List.of());

        var result = repository.findById(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllReturnsTrainers() {
        TrainerEntity trainer = trainer(2L, true);
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(trainer));

        List<TrainerEntity> result = repository.findAll();

        assertEquals(List.of(trainer), result);
    }

    @Test
    void findRejectsInvalidIdentifiers() {
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(IncorrectMethodArgumentException.class, () -> repository.findByUserName(" "));
    }

    @Test
    void existsByIdReturnsTrueWhenTrainerExists() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 2L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        boolean exists = repository.existsById(2L);

        assertTrue(exists);
    }

    @Test
    void existsByIdReturnsFalseWhenTrainerMissing() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 2L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        boolean exists = repository.existsById(2L);

        assertFalse(exists);
    }

    @Test
    void existsByIdRejectsNull() {
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
    }

    @Test
    void deleteRemovesManagedTrainer() {
        TrainerEntity trainer = trainer(2L, true);
        TrainerEntity managed = trainer(2L, true);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(managed);

        repository.delete(trainer);

        verify(em).remove(managed);
    }

    @Test
    void deleteRejectsInvalidTrainer() {
        assertThrows(NullPointerException.class, () -> repository.delete(null));
        assertThrows(NullPointerException.class, () -> repository.delete(trainer(null, true)));
    }

    @Test
    void deleteRejectsMissingTrainer() {
        TrainerEntity trainer = trainer(2L, true);

        assertThrows(EntityNotFoundException.class, () -> repository.delete(trainer));

        verify(em, never()).remove(any());
    }

    @Test
    void deleteByUsernameRejectsBlankUsername() {
        assertThrows(IncorrectMethodArgumentException.class, () -> repository.deleteByUsername(""));
    }

    @Test
    void activateTrainerUpdatesManagedUser() {
        TrainerEntity trainer = trainer(2L, false);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(trainer);

        repository.changeProfileStatus(trainer, true);

        assertTrue(trainer.getUserEntity().getActive());
    }

    @Test
    void deactivateTrainerUpdatesManagedUser() {
        TrainerEntity trainer = trainer(2L, true);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(trainer);

        repository.changeProfileStatus(trainer, false);

        assertFalse(trainer.getUserEntity().getActive());
    }

    @Test
    void changeProfileStatusRejectsNullTrainer() {
        assertThrows(NullPointerException.class, () -> repository.changeProfileStatus(null, true));
    }

    @Test
    void changeProfileStatusRejectsMissingTrainer() {
        TrainerEntity trainer = trainer(2L, true);

        assertThrows(EntityNotFoundException.class, () -> repository.changeProfileStatus(trainer, false));
    }

    @Test
    void unassignedTrainersRejectsUnknownTrainee() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", "missing")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        assertThrows(EntityNotFoundException.class, () -> repository.findUnassignedTrainers("missing"));
    }

    @Test
    void unassignedTrainersRejectsBlankUsername() {
        assertThrows(IncorrectMethodArgumentException.class, () -> repository.findUnassignedTrainers(" "));
    }
}
