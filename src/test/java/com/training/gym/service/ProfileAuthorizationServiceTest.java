package com.training.gym.service;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.User;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.service.auth.ProfileAuthorizationService;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileAuthorizationServiceTest {
    private final TraineeRepository trainees = mock(TraineeRepository.class);
    private final TrainerRepository trainers = mock(TrainerRepository.class);
    private final TraineePersistenceMapper traineeMapper = mock(TraineePersistenceMapper.class);
    private final TrainerPersistenceMapper trainerMapper = mock(TrainerPersistenceMapper.class);
    private final ProfileAuthorizationService service =
            new ProfileAuthorizationService(trainees, trainers, traineeMapper, trainerMapper);
    private final UsernamePasswordAuthenticationToken auth = UsernamePasswordAuthenticationToken.authenticated("owner", null, List.of());

    private void trainee(String username) {
        var entity = new TraineeEntity();
        when(trainees.findById(1L)).thenReturn(Optional.of(entity));
        when(traineeMapper.toTraineeDomain(entity)).thenReturn(Trainee.builder().user(User.builder().userName(username).build()).build());
    }

    private void trainer(String username) {
        var entity = new TrainerEntity();
        when(trainers.findById(1L)).thenReturn(Optional.of(entity));
        when(trainerMapper.toTrainerDomain(entity)).thenReturn(Trainer.builder().user(User.builder().userName(username).build()).build());
    }

    @Test
    void shouldAllowTraineeToAccessOwnProfile() {
        // Arrange
        trainee("owner");
        // Act
        boolean allowed = service.isTraineeOwner(auth, 1L);
        // Assert
        assertTrue(allowed);
    }

    @Test
    void shouldRejectUserWithoutTraineeProfile() {
        // Arrange
        when(trainees.findById(1L)).thenReturn(Optional.empty());
        // Act
        boolean allowed = service.isTraineeOwner(auth, 1L);
        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldRejectAccessToOtherTrainee() {
        // Arrange
        trainee("other");
        // Act
        boolean allowed = service.isTraineeOwner(auth, 1L);
        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldRejectNullCredentialsBeforeAuthentication() {
        // Arrange
        String username = "owner";
        // Act
        boolean allowed = service.isOwner(null, username);
        // Assert
        assertFalse(allowed);
    }

    @Test
    void shouldAuthenticateTrainer() {
        // Arrange
        trainer("owner");
        // Act
        boolean allowed = service.isTrainerOwner(auth, 1L);
        // Assert
        assertTrue(allowed);
    }

    @Test
    void shouldRejectAccessToOtherTrainer() {
        // Arrange
        trainer("other");
        // Act
        boolean allowed = service.isTrainerOwner(auth, 1L);
        // Assert
        assertFalse(allowed);
    }
}
