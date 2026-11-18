package com.training.gym.persistence.repository;

import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.User;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.persistence.mapper.TraineePersistenceMapper;
import com.training.gym.persistence.repository.JpaTraineeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTraineeRepositoryTest {
    private static final long ID = 1L;
    private static final String USERNAME = "james.bond";

    @Mock EntityManager em;
    @Mock TraineePersistenceMapper mapper;
    @Mock Trainee domain;
    @Mock TraineeEntity entity;
    @Mock User user;
    @Mock UserEntity userEntity;
    @Mock TypedQuery<TraineeEntity> query;
    @Mock TypedQuery<Long> countQuery;
    private JpaTraineeRepository repository;


    @BeforeEach
    void setUpRepository() {
        repository = new JpaTraineeRepository(mapper);
        ReflectionTestUtils.setField(repository, "entityManager", em);
    }

    @Test
    void shouldSaveNewTrainee() {
        // Arrange
        Trainee newTrainee = Trainee.builder()
                .user(User.builder()
                        .firstName("James")
                        .lastName("Bond")
                        .userName("james.bond")
                        .build())
                .build();

        Trainee savedTrainee = Trainee.builder()
                .id(1L)
                .user(newTrainee.getUser())
                .build();

        when(mapper.toEntity(newTrainee)).thenReturn(entity);
        when(mapper.toTraineeDomain(entity)).thenReturn(savedTrainee);

        // Act
        Trainee result = repository.save(newTrainee);

        // Assert
        assertSame(savedTrainee, result);
        assertEquals(1L, result.getId());

        verify(mapper).toEntity(newTrainee);
        verify(em).persist(entity);
        verify(mapper).toTraineeDomain(entity);
    }

    @Test
    void shouldRejectNullTraineeOnSave() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(null));
        verifyNoInteractions(em, mapper);
    }

    @Test
    void shouldRejectPresetIdOnSave() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(domain));
        verifyNoInteractions(em, mapper);
    }

    @Test
    void shouldUpdateOnlyFirstAndLastNames() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(domain.getUser()).thenReturn(user);
        when(user.getFirstName()).thenReturn("James");
        when(user.getLastName()).thenReturn("Bond");
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(mapper.toTraineeDomain(entity)).thenReturn(domain);

        // Act
        Trainee result = repository.update(domain);

        // Assert
        assertSame(domain, result);
        verify(userEntity).setFirstName("James");
        verify(userEntity).setLastName("Bond");
        verify(userEntity, never()).setPassword(any());
        verify(em, never()).merge(any());
    }

    @Test
    void shouldRejectInvalidTraineeOnUpdate() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.update(null)),
                () -> assertThrows(NullPointerException.class, () -> repository.update(domain))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectTraineeWithoutUserOnUpdate() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.update(domain));
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectUpdatingMissingTrainee() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(domain.getUser()).thenReturn(user);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.update(domain));
    }

    @Test
    void shouldFindTraineeById() {
        // Arrange
        stubFindById(ID, List.of(entity));
        when(mapper.toTraineeDomain(entity)).thenReturn(domain);

        // Act
        Optional<Trainee> result = repository.findById(ID);

        // Assert
        assertSame(domain, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenTraineeIdNotFound() {
        // Arrange
        stubFindById(ID, List.of());

        // Act
        Optional<Trainee> result = repository.findById(ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullIdOnFind() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldFindTraineeByUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTraineeDomain(entity)).thenReturn(domain);

        // Act
        Optional<Trainee> result = repository.findByUserName(USERNAME);

        // Assert
        assertSame(domain, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyForUnknownUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act
        Optional<Trainee> result = repository.findByUserName(USERNAME);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectInvalidUsernameOnFind() {
        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByUserName(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByUserName("  "))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldReturnAllMappedTrainees() {
        // Arrange
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTraineeDomain(entity)).thenReturn(domain);

        // Act
        List<Trainee> result = repository.findAll();

        // Assert
        assertEquals(List.of(domain), result);
    }

    @Test
    void shouldReturnEmptyListWhenNoTraineesExist() {
        // Arrange
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act
        List<Trainee> result = repository.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckTraineeExistence() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", ID)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);

        // Act & Assert
        assertTrue(repository.existsById(ID));
        assertFalse(repository.existsById(ID));
    }

    @Test
    void shouldRejectNullExistenceId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
    }

    @Test
    void shouldDeleteManagedTrainee() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);

        // Act
        repository.delete(domain);

        // Assert
        verify(em).remove(entity);
    }

    @Test
    void shouldRejectInvalidTraineeOnDelete() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.delete(null)),
                () -> assertThrows(EntityNotFoundException.class, () -> repository.delete(domain))
        );
    }

    @Test
    void shouldRejectDeletingMissingTrainee() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.delete(domain));
        verify(em, never()).remove(any());
    }

    @Test
    void shouldDeleteTraineeByUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTraineeDomain(entity)).thenReturn(domain);
        when(domain.getId()).thenReturn(ID);
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);

        // Act
        repository.deleteByUsername(USERNAME);

        // Assert
        verify(em).remove(entity);
    }

    @Test
    void shouldRejectUnknownUsernameOnDelete() {
        // Arrange
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("username", USERNAME)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.deleteByUsername(USERNAME));
    }

    @Test
    void shouldRejectBlankUsernameOnDelete() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.deleteByUsername(" "));
    }

    @Test
    void shouldActivateInactiveTrainee() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(userEntity.getActive()).thenReturn(false);

        // Act
        repository.activateTrainee(domain);

        // Assert
        verify(userEntity).setActive(true);
    }

    @Test
    void shouldDeactivateActiveTrainee() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(userEntity.getActive()).thenReturn(true);

        // Act
        repository.deactivateTrainee(domain);

        // Assert
        verify(userEntity).setActive(false);
    }

    @Test
    void shouldRejectRepeatedActivationOrDeactivation() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);
        when(entity.getUserEntity()).thenReturn(userEntity);
        when(userEntity.getActive()).thenReturn(true, false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> repository.activateTrainee(domain));
        assertThrows(IllegalStateException.class, () -> repository.deactivateTrainee(domain));
        verify(userEntity, never()).setActive(anyBoolean());
    }

    @Test
    void shouldRejectActivationWhenTraineeMissing() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.activateTrainee(domain));
    }

    @Test
    void shouldRejectNullTraineeDuringActivationOrDeactivation() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.activateTrainee(null)),
                () -> assertThrows(NullPointerException.class, () -> repository.deactivateTrainee(null))
        );
    }

    @Test
    void shouldReplaceAssignedTrainers() {
        // Arrange
        TrainerEntity first = mock(TrainerEntity.class);
        TrainerEntity second = mock(TrainerEntity.class);
        Set<TrainerEntity> managedAssignments = new HashSet<>();
        managedAssignments.add(first);
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(second);
        when(entity.getTrainerEntities()).thenReturn(managedAssignments);

        // Act
        repository.replaceTrainers(ID, Set.of(2L));

        // Assert
        assertEquals(Set.of(second), managedAssignments);
        verify(em).find(TrainerEntity.class, 2L);
    }

    @Test
    void shouldClearAllAssignedTrainers() {
        // Arrange
        Set<TrainerEntity> assigned = new HashSet<>();
        assigned.add(mock(TrainerEntity.class));
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);
        when(entity.getTrainerEntities()).thenReturn(assigned);

        // Act
        repository.replaceTrainers(ID, Set.of());

        // Assert
        assertTrue(assigned.isEmpty());
    }

    @Test
    void shouldRejectMissingTraineeForReplacement() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.replaceTrainers(ID, Set.of()));
    }

    @Test
    void shouldRejectMissingTrainerForReplacementWithoutUpdatingAssignments() {
        // Arrange
        when(em.find(TraineeEntity.class, ID)).thenReturn(entity);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.replaceTrainers(ID, Set.of(2L)));
        verify(entity, never()).getTrainerEntities();
    }

    @Test
    void shouldRejectNullReplacementArguments() {
        // Act & Assert
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> repository.replaceTrainers(null, Set.of())),
                () -> assertThrows(NullPointerException.class, () -> repository.replaceTrainers(ID, null))
        );
        verifyNoInteractions(em);
    }

    private void stubFindById(Long id, List<TraineeEntity> results) {
        when(em.createQuery(anyString(), eq(TraineeEntity.class))).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);
        when(query.getResultList()).thenReturn(results);
    }
}
