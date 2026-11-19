package com.training.gym.persistence.repository;

import com.training.gym.domain.model.TrainingType;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaTrainingTypeRepositoryTest {
    @Mock EntityManager em;
    @Mock TrainingTypePersistenceMapper mapper;
    @Mock TrainingTypeEntity entity;
    @Mock TrainingType domain;
    @Mock TypedQuery<TrainingTypeEntity> query;
    private JpaTrainingTypeRepository repository;


    @BeforeEach
    void setUpRepository() {
        repository = new JpaTrainingTypeRepository(mapper);
        ReflectionTestUtils.setField(repository, "entityManager", em);
    }

    @Test
    void shouldFindTrainingTypeById() {
        // Arrange
        when(em.find(TrainingTypeEntity.class, 3L)).thenReturn(entity);
        when(mapper.toTrainingTypeDomain(entity)).thenReturn(domain);

        // Act
        Optional<TrainingType> actual = repository.findById(3L);

        // Assert
        assertSame(domain, actual.orElseThrow());
    }

    @Test
    void shouldReturnEmptyForUnknownTrainingType() {
        // Act
        Optional<TrainingType> actual = repository.findById(999L);

        // Assert
        assertTrue(actual.isEmpty());
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldRejectNullTrainingTypeId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldReturnMappedTrainingTypes() {
        // Arrange
        TrainingTypeEntity otherEntity = mock(TrainingTypeEntity.class);
        TrainingType otherDomain = mock(TrainingType.class);
        when(em.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(entity, otherEntity));
        when(mapper.toTrainingTypeDomain(entity)).thenReturn(domain);
        when(mapper.toTrainingTypeDomain(otherEntity)).thenReturn(otherDomain);

        // Act
        List<TrainingType> actual = repository.findAll();

        // Assert
        assertEquals(List.of(domain, otherDomain), actual);
        verify(em).createQuery(contains("ORDER BY t.name"), eq(TrainingTypeEntity.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoTypesExist() {
        // Arrange
        when(em.createQuery(anyString(), eq(TrainingTypeEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        // Act
        List<TrainingType> actual = repository.findAll();

        // Assert
        assertTrue(actual.isEmpty());
        verifyNoInteractions(mapper);
    }
}
