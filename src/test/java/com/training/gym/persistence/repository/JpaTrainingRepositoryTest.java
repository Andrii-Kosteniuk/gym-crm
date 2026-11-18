package com.training.gym.persistence.repository;

import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.Training;
import com.training.gym.domain.TrainingType;
import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.mapper.TrainingPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTrainingRepositoryTest {
    private static final long ID = 10L;
    private static final long TRAINEE_ID = 1L;
    private static final long TRAINER_ID = 2L;
    private static final long TYPE_ID = 3L;
    private static final String TRAINEE_USERNAME = "james.bond";
    private static final String TRAINER_USERNAME = "sara.connor";

    @Mock
    EntityManager em;
    @Mock
    TrainingPersistenceMapper mapper;
    @Mock
    Training domain;
    @Mock
    TrainingEntity entity;
    @Mock
    Trainee trainee;
    @Mock
    Trainer trainer;
    @Mock
    TrainingType type;
    @Mock
    TraineeEntity traineeEntity;
    @Mock
    TrainerEntity trainerEntity;
    @Mock
    TrainingTypeEntity typeEntity;
    @Mock
    TypedQuery<TrainingEntity> query;
    @Mock
    TypedQuery<Long> countQuery;
    private JpaTrainingRepository repository;


    @BeforeEach
    void setUpRepository() {
        repository = new JpaTrainingRepository(mapper);
        ReflectionTestUtils.setField(repository, "entityManager", em);
    }

    @Test
    void shouldSaveNewTrainingAndSetGeneratedId() {
        // Arrange
        when(domain.getId()).thenReturn(null);

        stubValidReferences();

        when(mapper.toTrainingEntity(domain))
                .thenReturn(entity);

        when(entity.getId())
                .thenReturn(ID);

        // Act
        Training result = repository.save(domain);

        // Assert
        assertSame(domain, result);

        InOrder order = inOrder(entity, em, domain);

        order.verify(entity).setTraineeEntity(traineeEntity);
        order.verify(entity).setTrainerEntity(trainerEntity);
        order.verify(entity).setTrainingTypeEntity(typeEntity);

        order.verify(em).persist(entity);
        order.verify(domain).setId(ID);
    }

    @Test
    void shouldRejectNullTrainingOnSave() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.save(null));
        verifyNoInteractions(em, mapper);
    }

    @Test
    void shouldRejectTrainingWithPresetId() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(domain));
        verifyNoInteractions(em, mapper);
    }


    @Test
    void shouldRejectMissingTrainerIdOnSave() {
        // Arrange

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> repository.save(domain));
        assertEquals("New training must not have an ID", exception.getMessage());
        verify(em, never()).persist(any());
    }

    @Test
    void shouldRejectMissingTrainingTypeIdOnSave() {
        // Arrange


        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> repository.save(domain));
        assertEquals("New training must not have an ID", exception.getMessage());
        verify(em, never()).persist(any());
    }

    @Test
    void shouldRejectUnknownTraineeReference() {
        // Arrange

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(domain));
        verify(em, never()).persist(any());
    }

    @Test
    void shouldRejectUnknownTrainerReference() {
        // Arrange

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(domain));
        verify(em, never()).persist(any());
    }

    @Test
    void shouldRejectUnknownTrainingTypeReference() {
        // Arrange

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> repository.save(domain));
        verify(em, never()).persist(any());
    }

    @Test
    void shouldFindTrainingById() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.setParameter("id", ID)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTrainingDomain(entity)).thenReturn(domain);

        // Act
        Optional<Training> result = repository.findById(ID);

        // Assert
        assertSame(domain, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenTrainingIdUnknown() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.setParameter("id", ID)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act
        Optional<Training> result = repository.findById(ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullTrainingIdOnFind() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldReturnAllTrainingsInRepositoryOrder() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity));
        when(mapper.toTrainingDomain(entity)).thenReturn(domain);

        // Act
        List<Training> result = repository.findAll();

        // Assert
        assertEquals(List.of(domain), result);
        verify(em).createQuery(contains("ORDER BY t.trainingDate DESC"), eq(TrainingEntity.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainingsExist() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act
        List<Training> result = repository.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckTrainingExistence() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", ID)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);

        // Act & Assert
        assertTrue(repository.existsById(ID));
        assertFalse(repository.existsById(ID));
    }

    @Test
    void shouldRejectNullTrainingIdOnExistenceCheck() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.existsById(null));
    }

    @Test
    void shouldRejectBlankUsernameForTraineeSearch() {
        // Arrange
        TraineeTrainingFilter filter = mock(TraineeTrainingFilter.class);

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByTraineeUsername(null, filter)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByTraineeUsername(" ", filter))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectNullTraineeFilter() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> repository.findByTraineeUsername(TRAINEE_USERNAME, null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectBlankUsernameForTrainerSearch() {
        // Arrange
        TrainerTrainingFilter filter = mock(TrainerTrainingFilter.class);

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByTrainerUsername(null, filter)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByTrainerUsername(" ", filter))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectNullTrainerFilter() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> repository.findByTrainerUsername(TRAINER_USERNAME, null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldFindTraineeTrainingsWithAllOptionalFilters() {
        // Arrange
        TraineeTrainingFilter filter = mock(TraineeTrainingFilter.class);
        when(filter.fromDate()).thenReturn(LocalDate.of(2026, 11, 1));
        when(filter.toDate()).thenReturn(LocalDate.of(2026, 11, 30));
        when(filter.trainerName()).thenReturn("Sara Connor");
        when(filter.trainingType()).thenReturn("Fitness");
        stubCriteriaQuery();

        // Act
        List<Training> result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(domain), result);
        verify(em).getCriteriaBuilder();
    }

    @Test
    void shouldFindTraineeTrainingsWithNoOptionalFilters() {
        // Arrange
        TraineeTrainingFilter filter = mock(TraineeTrainingFilter.class);
        stubCriteriaQuery();

        // Act
        List<Training> result = repository.findByTraineeUsername(TRAINEE_USERNAME, filter);

        // Assert
        assertEquals(List.of(domain), result);
    }

    @Test
    void shouldFindTrainerTrainingsWithAllOptionalFilters() {
        // Arrange
        TrainerTrainingFilter filter = mock(TrainerTrainingFilter.class);
        when(filter.fromDate()).thenReturn(LocalDate.of(2026, 11, 1));
        when(filter.toDate()).thenReturn(LocalDate.of(2026, 11, 30));
        when(filter.traineeName()).thenReturn("James Bond");
        stubCriteriaQuery();

        // Act
        List<Training> result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(domain), result);
        verify(em).getCriteriaBuilder();
    }

    @Test
    void shouldFindTrainerTrainingsWithNoOptionalFilters() {
        // Arrange
        TrainerTrainingFilter filter = mock(TrainerTrainingFilter.class);
        stubCriteriaQuery();

        // Act
        List<Training> result = repository.findByTrainerUsername(TRAINER_USERNAME, filter);

        // Assert
        assertEquals(List.of(domain), result);
    }

    private void stubIds() {
        when(domain.getTrainee()).thenReturn(trainee);
        when(trainee.getId()).thenReturn(TRAINEE_ID);
        when(domain.getTrainer()).thenReturn(trainer);
        when(trainer.getId()).thenReturn(TRAINER_ID);
        when(domain.getTrainingType()).thenReturn(type);
        when(type.getId()).thenReturn(TYPE_ID);
    }

    private void stubValidReferences() {
        stubIds();
        when(em.find(TraineeEntity.class, TRAINEE_ID)).thenReturn(traineeEntity);
        when(em.find(TrainerEntity.class, TRAINER_ID)).thenReturn(trainerEntity);
        when(em.find(TrainingTypeEntity.class, TYPE_ID)).thenReturn(typeEntity);
    }

    @SuppressWarnings("unchecked")
    private void stubCriteriaQuery() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        CriteriaQuery<TrainingEntity> cq = mock(CriteriaQuery.class, RETURNS_DEEP_STUBS);
        Root<TrainingEntity> root = mock(Root.class, RETURNS_DEEP_STUBS);
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(TrainingEntity.class)).thenReturn(cq);
        when(cq.from(TrainingEntity.class)).thenReturn(root);
        when(em.createQuery(cq)).thenReturn(query);
        when(query.getResultStream()).thenAnswer(inv -> List.of(entity).stream());
        when(mapper.toTrainingDomain(entity)).thenReturn(domain);
    }
}
