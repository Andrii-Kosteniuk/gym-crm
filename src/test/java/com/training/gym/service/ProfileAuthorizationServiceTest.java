package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.exception.AccessDeniedException;
import com.training.gym.exception.AuthenticationFailedException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileAuthorizationServiceTest {
    @Mock
    CredentialService credentials;
    @Mock
    TraineeRepository trainees;
    @Mock
    TrainerRepository trainers;
    @InjectMocks
    ProfileAuthorizationService service;

    private com.training.gym.dto.auth.Credentials jamesCredentials() {
        return new com.training.gym.dto.auth.Credentials("james.bond", "secret");
    }

    private User james() {
        return User.builder().id(1L).firstName("James").lastName("Bond").userName("james.bond").build();
    }

    @Test
    void shouldAuthenticateTrainee() {
        // Arrange
        User user = james();
        Trainee trainee = Trainee.builder().user(user).build();
        when(credentials.authenticate("james.bond", "secret")).thenReturn(user);
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(trainee));
        // Act
        Trainee result = service.requireTrainee(jamesCredentials());
        // Assert
        assertSame(trainee, result);
    }

    @Test
    void shouldRejectUserWithoutTraineeProfile() {
        // Arrange
        when(credentials.authenticate("james.bond", "secret")).thenReturn(james());
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(AuthenticationFailedException.class, () -> service.requireTrainee(jamesCredentials()));
    }

    @Test
    void shouldRejectAccessToOtherTrainee() {
        // Arrange
        User user = james();
        when(credentials.authenticate("james.bond", "secret")).thenReturn(user);
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(Trainee.builder().user(user).build()));
        // Act / Assert
        assertThrows(AccessDeniedException.class, () -> service.requireTraineeOwner(jamesCredentials(), "jym.kerry"));
    }

    @Test
    void shouldRejectNullCredentialsBeforeAuthentication() {
        // Arrange
        com.training.gym.dto.auth.Credentials missing = null;
        // Act / Assert
        assertThrows(NullPointerException.class, () -> service.requireTrainee(missing));
        verifyNoInteractions(credentials);
    }

    @Test
    void shouldAuthenticateTrainer() {
        // Arrange
        User sara = User.builder().userName("sara.connor").build();
        Trainer trainer = Trainer.builder().user(sara).build();
        when(credentials.authenticate("sara.connor", "secret")).thenReturn(sara);
        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(trainer));
        // Act
        Trainer result = service.requireTrainer(new com.training.gym.dto.auth.Credentials("sara.connor", "secret"));
        // Assert
        assertSame(trainer, result);
    }

    @Test
    void shouldRejectAccessToOtherTrainer() {
        // Arrange
        User sara = User.builder().userName("sara.connor").build();
        when(credentials.authenticate("sara.connor", "secret")).thenReturn(sara);
        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(Trainer.builder().user(sara).build()));
        // Act / Assert
        assertThrows(AccessDeniedException.class, () -> service.requireTrainerOwner(new com.training.gym.dto.auth.Credentials("sara.connor", "secret"), "jym.kerry"));
    }
}
