package com.training.gym.service;

import com.training.gym.domain.model.User;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.UserPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {
    @Mock
    UserRepository users;
    @Mock
    PasswordEncoder encoder;
    @Mock
    UserPersistenceMapper mapper;
    @InjectMocks
    CredentialServiceImpl service;

    private UserEntity stored() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUserName("james.bond");
        entity.setPassword("hash");
        entity.setActive(true);
        return entity;
    }

    @Test
    void authenticateReturnsDomainNotJpaEntity() {
        UserEntity stored = stored();
        User result = User.builder().id(1L).password("hash").build();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(stored));
        when(encoder.matches("secret", "hash")).thenReturn(true);
        when(mapper.toUserDomain(stored)).thenReturn(result);
        assertSame(result, service.authenticate("james.bond", "secret"));
    }

    @Test
    void authenticationRejectsInvalidCredentials() {
        for (String invalid : new String[]{"", " "}) {
            assertThrows(AuthenticationFailedException.class, () -> service.authenticate(invalid, "password"));
            assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", invalid));
        }
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate(null, "password"));
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", null));
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("missing", "password"));
    }

    @Test
    void authenticationRejectsInactiveAccount() {
        UserEntity entity = stored();
        entity.setActive(false);
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(entity));
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", "secret"));
        verifyNoInteractions(encoder, mapper);
    }

    @Test
    void authenticationRejectsIncorrectPassword() {
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(stored()));
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", "bad"));
        verifyNoInteractions(mapper);
    }

    @Test
    void changePasswordUpdatesJpaEntityThroughRepository() {
        UserEntity entity = stored();
        User domain = User.builder().id(1L).password("hash").build();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(entity));
        when(encoder.matches("old-pass", "hash")).thenReturn(true);
        when(mapper.toUserDomain(entity)).thenReturn(domain);
        when(encoder.matches("new-pass", "hash")).thenReturn(false);
        when(encoder.encode("new-pass")).thenReturn("new-hash");
        service.changePassword("james.bond", "old-pass", "new-pass");
        verify(users).updatePassword(1L, "new-hash");
    }

    @Test
    void changePasswordRejectsWeakAndReusedPassword() {
        assertThrows(IncorrectMethodArgumentException.class, () -> service.changePassword("james.bond", "old", null));
        assertThrows(IncorrectMethodArgumentException.class, () -> service.changePassword("james.bond", "old", "short"));
        UserEntity entity = stored();
        User domain = User.builder().id(1L).password("hash").build();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(entity));
        when(encoder.matches("old-pass", "hash")).thenReturn(true);
        when(encoder.matches("new-pass", "hash")).thenReturn(true);
        when(mapper.toUserDomain(entity)).thenReturn(domain);
        assertThrows(IncorrectMethodArgumentException.class, () -> service.changePassword("james.bond", "old-pass", "new-pass"));
        verify(users, never()).updatePassword(anyLong(), anyString());
    }
}
