package com.training.gym.persistence.repository;

import com.training.gym.domain.User;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.persistence.mapper.UserPersistenceMapper;
import com.training.gym.persistence.repository.JpaUserRepository;
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
class JpaUserRepositoryTest {
    private static final long ID = 1L;
    private static final String USERNAME = "james.bond";

    @Mock EntityManager em;
    @Mock UserPersistenceMapper mapper;
    @Mock UserEntity entity;
    @Mock User domain;
    @Mock TypedQuery<Long> countQuery;
    @Mock TypedQuery<UserEntity> userQuery;
    private JpaUserRepository repository;


    @BeforeEach
    void setUpRepository() {
        repository = new JpaUserRepository(mapper);
        ReflectionTestUtils.setField(repository, "entityManager", em);
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", USERNAME)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);

        // Act
        boolean exists = repository.existsByUsername(USERNAME);

        // Assert
        assertTrue(exists);
        verify(countQuery).setParameter("username", USERNAME);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        // Arrange
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", USERNAME)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        // Act
        boolean exists = repository.existsByUsername(USERNAME);

        // Assert
        assertFalse(exists);
    }

    @Test
    void shouldFindUserByUsername() {
        // Arrange
        when(em.createQuery(anyString(), eq(UserEntity.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", USERNAME)).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of(entity));
        when(mapper.toUserDomain(entity)).thenReturn(domain);

        // Act
        Optional<User> result = repository.findByUsername(USERNAME);

        // Assert
        assertSame(domain, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // Arrange
        when(em.createQuery(anyString(), eq(UserEntity.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", USERNAME)).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of());

        // Act
        Optional<User> result = repository.findByUsername(USERNAME);

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldRejectNullOrBlankUsernameWhenCheckingExistence() {
        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.existsByUsername(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.existsByUsername("  "))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectNullOrBlankUsernameWhenFindingUser() {
        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByUsername(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.findByUsername("  "))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldUpdateEncodedPassword() {
        // Arrange
        when(em.find(UserEntity.class, ID)).thenReturn(entity);

        // Act
        repository.updatePassword(ID, "new-hash");

        // Assert
        verify(entity).setPassword("new-hash");
    }

    @Test
    void shouldRejectNullUserIdForPasswordUpdate() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.updatePassword(null, "hash"));
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectBlankEncodedPassword() {
        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.updatePassword(ID, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.updatePassword(ID, " "))
        );
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectPasswordUpdateWhenUserMissing() {
        // Arrange: EntityManager.find returns null by default.

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.updatePassword(ID, "new-hash"));
    }

    @Test
    void shouldDeleteManagedUser() {
        // Arrange
        when(domain.getId()).thenReturn(ID);
        when(em.find(UserEntity.class, ID)).thenReturn(entity);

        // Act
        repository.delete(domain);

        // Assert
        verify(em).remove(entity);
    }

    @Test
    void shouldRejectNullUserOnDelete() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.delete(null));
        verifyNoInteractions(em);
    }

    @Test
    void shouldRejectUserWithoutIdOnDelete() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.delete(domain));
    }

    @Test
    void shouldRejectDeletingMissingUser() {
        // Arrange
        when(domain.getId()).thenReturn(ID);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> repository.delete(domain));
        verify(em, never()).remove(any());
    }
}
