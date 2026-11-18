package com.training.gym.service;

import com.training.gym.dto.auth.Credentials;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.User;
import com.training.gym.exception.AccessDeniedException;
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
class ProfileAuthorizationServiceTest {
    @Mock
    CredentialService credentialService;
    @Mock
    TraineeRepository trainees;
    @Mock
    TrainerRepository trainers;
    @InjectMocks
    ProfileAuthorizationService service;

    private Credentials jamesCredentials() {
        return new Credentials("james.bond", "secret");
    }

    private User james() {
        return User.builder()
                .id(1L)
                .firstName("James")
                .lastName("Bond")
                .userName("james.bond")
                .build();
    }

    @Test
    void shouldAllowTraineeToAccessOwnProfile() {
        // Arrange
        Long traineeId = 1L;

        User user = james();

        Trainee trainee = Trainee.builder()
                .id(traineeId)
                .user(user)
                .build();

        Credentials credentials = jamesCredentials();

        when(credentialService.authenticate("james.bond", "secret"))
                .thenReturn(user);

        when(trainees.findByUserName("james.bond"))
                .thenReturn(Optional.of(trainee));

        // Act & Assert
        assertDoesNotThrow(() -> service.requireTraineeOwner(credentials, traineeId));

        verify(credentialService).authenticate("james.bond", "secret");

        verify(trainees).findByUserName("james.bond");
    }

    @Test
    void shouldRejectUserWithoutTraineeProfile() {
        // Arrange
        when(credentialService.authenticate("james.bond", "secret")).thenReturn(james());
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(AuthenticationFailedException.class, () -> service.requireTraineeOwner(jamesCredentials(),  james().getId()));
    }

    @Test
    void shouldRejectAccessToOtherTrainee() {
        // Arrange
        User user = james();
        when(credentialService.authenticate("james.bond", "secret")).thenReturn(user);
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(Trainee.builder().user(user).build()));
        // Act / Assert
        assertThrows(AccessDeniedException.class, () -> service.requireTraineeOwner(jamesCredentials(),  james().getId()));
    }

    @Test
    void shouldRejectNullCredentialsBeforeAuthentication() {
        // Arrange
        Credentials missing = null;
        // Act / Assert
        assertThrows(AuthenticationFailedException.class, () -> service.requireTraineeOwner(missing, james().getId()));
        verifyNoInteractions(credentialService);
    }

    @Test
    void shouldAuthenticateTrainer() {
        // Arrange
        User sara = User.builder().userName("sara.connor").build();
        Trainer trainer = Trainer.builder().user(sara).build();
        when(credentialService.authenticate("sara.connor", "secret")).thenReturn(sara);
        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(trainer));
        // Act
        service.requireTrainerOwner(new Credentials("sara.connor", "secret"), sara.getId());
        // Assert
        assertDoesNotThrow(()-> new AuthenticationFailedException("Authentication Failed"));
    }

    @Test
    void shouldRejectAccessToOtherTrainer() {
        // Arrange
        User sara = User.builder().userName("sara.connor").build();
        when(credentialService.authenticate("sara.connor", "secret")).thenReturn(sara);
        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(Trainer.builder().user(sara).build()));
        // Act / Assert
        assertThrows(AccessDeniedException.class, () -> service.requireTrainerOwner(new Credentials("sara.connor", "secret"), james().getId()));
    }
}
