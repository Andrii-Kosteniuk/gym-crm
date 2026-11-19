package com.training.gym.persistence.repository;

import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.domain.model.User;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTrainerRepositoryTest {
    private static final long ID = 2L;
    private static final long TYPE_ID = 3L;
    private static final String USERNAME = "sara.connor";

    @Mock EntityManager em;
    @Mock TrainerPersistenceMapper mapper;
    @Mock Trainer domain;
    @Mock TrainerEntity entity;
    @Mock TrainingType type;
    @Mock TrainingTypeEntity typeEntity;
    @Mock User user;
    @Mock UserEntity userEntity;
    @Mock TypedQuery<TrainerEntity> query;
    @Mock TypedQuery<Long> countQuery;
    private JpaTrainerRepository repository;


    @BeforeEach
    void setUpRepository() {
        repository = new JpaTrainerRepository(mapper);
        ReflectionTestUtils.setField(repository, "entityManager", em);
    }

    @Test
    void shouldSaveTrainerWithManagedSpecialization() {
        // Arrange
        when(domain.getId()).thenReturn(null);
        when(domain.getSpecialization()).thenReturn(type);
        when(type.getId()).thenReturn(TYPE_ID);

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(em.find(TrainingTypeEntity.class, TYPE_ID))
                .thenReturn(typeEntity);
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);

        // Act
        Trainer result = repository.save(domain);

        // Assert
        assertSame(domain, result);

        verify(mapper).toEntity(domain);
        verify(em).find(TrainingTypeEntity.class, TYPE_ID);

        InOrder order = inOrder(entity, em);

        order.verify(entity).setSpecialization(typeEntity);
        order.verify(em).persist(entity);

        verify(mapper).toTrainerDomain(entity);
    }

    @Test
    void shouldRejectNullTrainerOnSave() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(null));
        verifyNoInteractions(em, mapper);
    }

    @Test
    void shouldRejectTrainerWithPresetIdOnSave() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(domain));
    }

    @Test
    void shouldUpdateNamesAndSpecialization() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(domain.getUser()).thenReturn(user);
        when(user.getFirstName()).thenReturn("Sara");
        when(user.getLastName()).thenReturn("Connor");
        when(domain.getSpecialization()).thenReturn(type);
        when(type.getId()).thenReturn(TYPE_ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);
        when(em.find(TrainingTypeEntity.class, TYPE_ID)).thenReturn(typeEntity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);

        // Act
        Trainer result = repository.update(domain);

        // Assert
        assertSame(domain, result);
        verify(userEntity).setFirstName("Sara");
        verify(userEntity).setLastName("Connor");
        verify(userEntity, never()).setPassword(any());
        verify(entity).setSpecialization(typeEntity);
    }

    @Test
    void shouldRejectNullOrIdlessTrainerOnUpdate() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.update(null)),
                () -> assertThrows(EntityNotFoundException.class, () -> repository.update(domain))
        );
    }

    @Test
    void shouldRejectMissingTrainerOnUpdate() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.update(domain));
    }

    @Test
    void shouldRejectUnknownTypeOnUpdate() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(domain.getUser()).thenReturn(user);
        when(domain.getSpecialization()).thenReturn(type);
        when(type.getId()).thenReturn(TYPE_ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.update(domain));
        verify(entity, never()).setSpecialization(any());
    }

    @Test
    void shouldFindTrainerById() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("id", ID)).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.of(entity).stream());
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);

        // Act
        Optional<Trainer> result = repository.findById(ID);

        // Assert
        assertSame(domain, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("id", ID)).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.<TrainerEntity>of().stream());

        // Act
        Optional<Trainer> result = repository.findById(ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullIdOnFind() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.findById(null));
    }

    @Test
    void shouldFindTrainerByUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);

        // Act
        Optional<Trainer> result = repository.findByUserName(USERNAME);

        // Assert
        assertSame(domain, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyForUnknownUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act
        Optional<Trainer> result = repository.findByUserName(USERNAME);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectInvalidUsernameOnFind() {
        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByUserName(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByUserName(" "))
        );
    }

    @Test
    void shouldFindAllMappedTrainers() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);

        // Act
        List<Trainer> result = repository.findAll();

        // Assert
        assertEquals(List.of(domain), result);
    }

    @Test
    void shouldCheckTrainerExistence() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", ID)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);

        // Act & Assert
        assertTrue(repository.existsById(ID));
        assertFalse(repository.existsById(ID));
    }

    @Test
    void shouldRejectNullIdOnExists() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
    }

    @Test
    void shouldDeleteManagedTrainer() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);

        // Act
        repository.delete(domain);

        // Assert
        verify(em).remove(entity);
    }

    @Test
    void shouldRejectNullOrIdlessTrainerOnDelete() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.delete(null)),
                () -> assertThrows(EntityNotFoundException.class, () -> repository.delete(domain))
        );
    }

    @Test
    void shouldRejectDeletingMissingTrainer() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.delete(domain));
        verify(em, never()).remove(any());
    }

    @Test
    void shouldDeleteTrainerByUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);
        when(domain.getId()).thenReturn(ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);

        // Act
        repository.deleteByUsername(USERNAME);

        // Assert
        verify(em).remove(entity);
    }

    @Test
    void shouldRejectUnknownUsernameOnDelete() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.deleteByUsername(USERNAME));
    }

    @Test
    void shouldRejectInvalidUsernameOnDelete() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.deleteByUsername(" "));
    }

    @Test
    void shouldActivateInactiveTrainer() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(userEntity.getActive()).thenReturn(false);

        // Act
        repository.activateTrainer(domain);

        // Assert
        verify(userEntity).setActive(true);
    }

    @Test
    void shouldDeactivateActiveTrainer() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(userEntity.getActive()).thenReturn(true);

        // Act
        repository.deactivateTrainer(domain);

        // Assert
        verify(userEntity).setActive(false);
    }

    @Test
    void shouldRejectRepeatedActivationAndDeactivation() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TrainerEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(userEntity.getActive()).thenReturn(true, false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> repository.activateTrainer(domain));
        assertThrows(IllegalStateException.class, () -> repository.deactivateTrainer(domain));
        verify(userEntity, never()).setActive(anyBoolean());
    }

    @Test
    void shouldRejectMissingTrainerOnActivation() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.activateTrainer(domain));
    }

    @Test
    void shouldRejectNullTrainerOnActivationAndDeactivation() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.activateTrainer(null)),
                () -> assertThrows(NullPointerException.class, () -> repository.deactivateTrainer(null))
        );
    }

    @Test
    void shouldReturnUnassignedTrainers() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", USERNAME)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        when(em.createQuery(anyString(), eq(TrainerEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.of(entity).stream());
        when(mapper.toTrainerDomain(entity)).thenReturn(domain);

        // Act
        List<Trainer> result = repository.findUnassignedTrainers(USERNAME);

        // Assert
        assertEquals(List.of(domain), result);
        verify(em).createQuery(contains("NOT EXISTS"), eq(TrainerEntity.class));
    }

    @Test
    void shouldRejectUnassignedQueryForMissingTrainee() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", USERNAME)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.findUnassignedTrainers(USERNAME));
        verify(em, never()).createQuery(anyString(), eq(TrainerEntity.class));
    }

    @Test
    void shouldRejectInvalidUsernameForUnassignedQuery() {
        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findUnassignedTrainers(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findUnassignedTrainers(" "))
        );
    }
}
