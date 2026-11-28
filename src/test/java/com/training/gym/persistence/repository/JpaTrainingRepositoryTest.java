package com.training.gym.persistence.repository;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTrainingRepositoryTest {

    @Mock
    EntityManager em;

    @Mock
    TypedQuery<TrainingEntity> query;

    @Mock
    TypedQuery<Long> countQuery;

    JpaTrainingRepository trainingRepository;

    @BeforeEach
    void setUp() {
        trainingRepository = new JpaTrainingRepository();
        ReflectionTestUtils.setField(trainingRepository, "entityManager", em);
    }

    private TrainingEntity training(Long id) {
        TrainingEntity training = new TrainingEntity();
        training.setId(id);

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        training.setTraineeEntity(trainee);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(2L);
        training.setTrainerEntity(trainer);

        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(3L);
        training.setTrainingTypeEntity(type);

        return training;
    }

    @Test
    void shouldSaveTrainingWithManagedReferences() {
        TrainingEntity training = training(null);
        TraineeEntity trainee = new TraineeEntity();
        TrainerEntity trainer = new TrainerEntity();
        TrainingTypeEntity type = new TrainingTypeEntity();
        when(em.find(TraineeEntity.class, 1L)).thenReturn(trainee);
        when(em.find(TrainerEntity.class, 2L)).thenReturn(trainer);
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(type);

        TrainingEntity result = trainingRepository.save(training);

        assertSame(training, result);
        assertSame(trainee, training.getTraineeEntity());
        assertSame(trainer, training.getTrainerEntity());
        assertSame(type, training.getTrainingTypeEntity());
        verify(em).persist(training);
    }

    @Test
    void shouldRejectNullTraining() {
        assertThrows(NullPointerException.class, () -> trainingRepository.save(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectTrainingWithExistingId() {
        TrainingEntity training = training(10L);

        assertThrows(IncorrectMethodArgumentException.class, () -> trainingRepository.save(training));

        verify(em, never()).persist(any());
    }

    @Test
    void shouldRejectTrainingWithoutTraineeId() {
        TrainingEntity training = training(null);
        training.setTraineeEntity(new TraineeEntity());

        assertThrows(NullPointerException.class, () -> trainingRepository.save(training));

        verify(em, never()).persist(any());
    }

    @Test
    void shouldRejectMissingTrainingReferences() {
        TrainingEntity training = training(null);

        assertThrows(EntityNotFoundException.class, () -> trainingRepository.save(training));

        verify(em, never()).persist(any());
    }

    @Test
    void shouldFindTrainingById() {
        TrainingEntity training = training(10L);
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.setParameter("id", 10L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(training));

        var result = trainingRepository.findById(10L);

        assertTrue(result.isPresent());
        assertSame(training, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenTrainingNotFound() {
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.setParameter("id", 10L)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        var result = trainingRepository.findById(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullTrainingId() {
        assertThrows(NullPointerException.class, () -> trainingRepository.findById(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldFindAllTrainings() {
        TrainingEntity training = training(10L);
        when(em.createQuery(anyString(), eq(TrainingEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(training));

        List<TrainingEntity> result = trainingRepository.findAll();

        assertEquals(List.of(training), result);
    }

    @Test
    void shouldReturnTrueWhenTrainingExists() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 10L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        boolean result = trainingRepository.existsById(10L);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenTrainingDoesNotExist() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", 10L)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        boolean result = trainingRepository.existsById(10L);

        assertFalse(result);
    }

    @Test
    void shouldRejectNullIdWhenCheckingExistence() {
        assertThrows(NullPointerException.class, () -> trainingRepository.existsById(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectBlankTraineeUsername() {
        TraineeTrainingFilter filter = new TraineeTrainingFilter(null, null, null, null);

        assertThrows(IncorrectMethodArgumentException.class,
                () -> trainingRepository.findByTraineeUsername(" ", filter));

        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectNullTrainerUsername() {
        TrainerTrainingFilter filter = new TrainerTrainingFilter(null, null, null);

        assertThrows(IncorrectMethodArgumentException.class,
                () -> trainingRepository.findByTrainerUsername(null, filter));

        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectNullTraineeFilter() {
        assertThrows(NullPointerException.class,
                () -> trainingRepository.findByTraineeUsername("james.bond", null));

        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectNullTrainerFilter() {
        assertThrows(NullPointerException.class,
                () -> trainingRepository.findByTrainerUsername("sara.connor", null));

        verifyNoInteractions(em);
    }

    @Nested
    class JpaTrainingRepositoryCriteriaTest {

        private final EntityManager entityManager = mock(EntityManager.class);
        private final CriteriaBuilder cb = mock(CriteriaBuilder.class, RETURNS_MOCKS);
        private final CriteriaQuery<TrainingEntity> cq = mock(CriteriaQuery.class, RETURNS_SELF);
        private final Root<TrainingEntity> root = mock(Root.class, RETURNS_DEEP_STUBS);
        private final TypedQuery<TrainingEntity> query = mock(TypedQuery.class);

        private JpaTrainingRepository repository;

        @BeforeEach
        void setUp() {
            repository = new JpaTrainingRepository();
            ReflectionTestUtils.setField(repository, "entityManager", entityManager);
        }

        private void stubCriteria(TrainingEntity... results) {
            when(entityManager.getCriteriaBuilder()).thenReturn(cb);
            when(cb.createQuery(TrainingEntity.class)).thenReturn(cq);
            when(cq.from(TrainingEntity.class)).thenReturn(root);
            when(entityManager.createQuery(cq)).thenReturn(query);
            when(query.getResultStream()).thenReturn(Stream.of(results));
        }

        private static TraineeTrainingFilter traineeFilter(
                LocalDate from, LocalDate to, String trainer, String type) {
            return new TraineeTrainingFilter(from, to, trainer, type);
        }

        private static TrainerTrainingFilter trainerFilter(
                LocalDate from, LocalDate to, String trainee) {
            return new TrainerTrainingFilter(from, to, trainee);
        }

        @Test
        void traineeSearchRejectsNullUsername() {
            TraineeTrainingFilter filter = traineeFilter(null, null, null, null);

            assertThrows(IncorrectMethodArgumentException.class,
                    () -> repository.findByTraineeUsername(null, filter));

            verifyNoInteractions(entityManager);
        }

        @Test
        void traineeSearchRejectsBlankUsername() {
            TraineeTrainingFilter filter = traineeFilter(null, null, null, null);

            assertThrows(IncorrectMethodArgumentException.class,
                    () -> repository.findByTraineeUsername("  ", filter));

            verifyNoInteractions(entityManager);
        }

        @Test
        void traineeSearchRejectsNullFilter() {
            assertThrows(NullPointerException.class,
                    () -> repository.findByTraineeUsername("james.bond", null));

            verifyNoInteractions(entityManager);
        }

        @Test
        void trainerSearchRejectsNullUsername() {
            TrainerTrainingFilter filter = trainerFilter(null, null, null);

            assertThrows(IncorrectMethodArgumentException.class,
                    () -> repository.findByTrainerUsername(null, filter));

            verifyNoInteractions(entityManager);
        }

        @Test
        void trainerSearchRejectsBlankUsername() {
            TrainerTrainingFilter filter = trainerFilter(null, null, null);

            assertThrows(IncorrectMethodArgumentException.class,
                    () -> repository.findByTrainerUsername("  ", filter));

            verifyNoInteractions(entityManager);
        }

        @Test
        void trainerSearchRejectsNullFilter() {
            assertThrows(NullPointerException.class,
                    () -> repository.findByTrainerUsername("sara.connor", null));

            verifyNoInteractions(entityManager);
        }
    }
}
