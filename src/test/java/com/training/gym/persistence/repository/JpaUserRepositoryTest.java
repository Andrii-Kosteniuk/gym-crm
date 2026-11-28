package com.training.gym.persistence.repository;

import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserNotFoundException;
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
class JpaUserRepositoryTest {
    @Mock
    EntityManager em;
    @Mock
    TypedQuery<UserEntity> userQuery;
    @Mock
    TypedQuery<Long> countQuery;
    JpaUserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository = new JpaUserRepository();
        ReflectionTestUtils.setField(userRepository, "entityManager", em);
    }

    @Test
    void existsByUsernameReturnsCount() {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("username", "james.bond")).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L, 0L);
        assertTrue(userRepository.existsByUsername("james.bond"));
        assertFalse(userRepository.existsByUsername("james.bond"));
    }

    @Test
    void findByUsernameReturnsJpaEntity() {
        UserEntity userEntity = new UserEntity();
        when(em.createQuery(anyString(), eq(UserEntity.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", "james.bond")).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of(userEntity), List.of());
        assertSame(userEntity, userRepository.findByUsername("james.bond").orElseThrow());
        assertTrue(userRepository.findByUsername("james.bond").isEmpty());
    }

    @Test
    void rejectsBlankUsername() {
        for (String value : new String[]{"", " "}) {
            assertThrows(IncorrectMethodArgumentException.class, () -> userRepository.existsByUsername(value));
            assertThrows(IncorrectMethodArgumentException.class, () -> userRepository.findByUsername(value));
        }
        assertThrows(IncorrectMethodArgumentException.class, () -> userRepository.existsByUsername(null));
        assertThrows(IncorrectMethodArgumentException.class, () -> userRepository.findByUsername(null));
    }

    @Test
    void updatePasswordModifiesManagedEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(em.find(UserEntity.class, 1L)).thenReturn(userEntity);
        userRepository.updatePassword(1L, "encoded");
        assertEquals("encoded", userEntity.getPassword());
        verify(em, never()).merge(any());
    }

    @Test
    void updatePasswordRejectsMissingOrInvalidInput() {
        assertThrows(NullPointerException.class, () -> userRepository.updatePassword(null, "encoded"));
        assertThrows(UserNotFoundException.class, () -> userRepository.updatePassword(1L, " "));
        assertThrows(UserNotFoundException.class, () -> userRepository.updatePassword(1L, "encoded"));
    }

    @Test
    void deleteRemovesManagedEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        UserEntity managedUser = new UserEntity();
        managedUser.setId(1L);
        when(em.find(UserEntity.class, 1L)).thenReturn(managedUser);
        userRepository.delete(userEntity);
        verify(em).remove(managedUser);
    }

    @Test
    void deleteRejectsInvalidOrMissingEntity() {
        assertThrows(NullPointerException.class, () -> userRepository.delete(null));
        assertThrows(NullPointerException.class, () -> userRepository.delete(new UserEntity()));
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        assertThrows(UserNotFoundException.class, () -> userRepository.delete(userEntity));
    }
}
