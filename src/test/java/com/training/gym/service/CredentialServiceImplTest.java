package com.training.gym.service;

import com.training.gym.repository.UserRepository;
import com.training.gym.domain.User;
import com.training.gym.exception.AuthenticationFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {
    @Mock
    UserRepository users;
    @Mock
    org.springframework.security.crypto.password.PasswordEncoder encoder;
    @InjectMocks
    CredentialServiceImpl service;

    private User james() {
        return User.builder().id(1L).firstName("James").lastName("Bond").userName("james.bond").password("hash").active(true).build();
    }

    @Test
    void shouldAuthenticateCorrectPassword() {
        // Arrange
        User james = james();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(james));
        when(encoder.matches("secret", "hash")).thenReturn(true);
        // Act
        User result = service.authenticate("james.bond", "secret");
        // Assert
        assertSame(james, result);
        verify(encoder).matches("secret", "hash");
    }

    @Test
    void shouldRejectUnknownAccount() {
        // Arrange
        when(users.findByUsername("james.bond")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", "secret"));
        verifyNoInteractions(encoder);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        // Arrange
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(james()));
        // Act / Assert
        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", "wrong"));
    }

    @Test
    void shouldRejectMissingCredentialsWithoutQuery() {
        // Arrange
        String[] invalid = {null, "", " "};
        // Act / Assert
        for (String value : invalid) {
            assertThrows(AuthenticationFailedException.class, () -> service.authenticate(value, "secret"));
            assertThrows(AuthenticationFailedException.class, () -> service.authenticate("james.bond", value));
        }
        verifyNoInteractions(users, encoder);
    }

    @Test
    void shouldReplacePasswordHash() {
        // Arrange
        User james = james();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(james));
        when(encoder.matches("old-secret", "hash")).thenReturn(true);
        when(encoder.matches("new-secret", "hash")).thenReturn(false);
        when(encoder.encode("new-secret")).thenReturn("new-hash");
        // Act
        service.changePassword("james.bond", "old-secret", "new-secret");
        // Assert
        verify(encoder).matches("old-secret", "hash");
        verify(encoder).matches("new-secret", "hash");
        verify(encoder).encode("new-secret");

        assertEquals("hash", james.getPassword());
        assertEquals("james.bond", james.getUserName());
    }

    @Test
    void shouldRejectShortNewPasswordBeforeAuthentication() {
        // Arrange
        String tooShort = "12345";
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.changePassword("james.bond", "old", tooShort));
        verifyNoInteractions(users, encoder);
    }

    @Test
    void shouldRejectReusedPassword() {
        // Arrange
        User james = james();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(james));
        when(encoder.matches("secret123", "hash")).thenReturn(true);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.changePassword("james.bond", "secret123", "secret123"));
        assertEquals("hash", james.getPassword());
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void shouldNotChangePasswordWhenOldPasswordIsWrong() {
        // Arrange
        User james = james();
        when(users.findByUsername("james.bond")).thenReturn(Optional.of(james));
        // Act / Assert
        assertThrows(AuthenticationFailedException.class, () -> service.changePassword("james.bond", "wrong", "new-secret"));
        assertEquals("hash", james.getPassword());
        verify(encoder, never()).encode(anyString());
    }
}
