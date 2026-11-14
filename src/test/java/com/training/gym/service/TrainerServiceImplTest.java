package com.training.gym.service;

import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.TrainingTypeRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.TrainingType;
import com.training.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
    @Mock
    TrainerRepository trainers;
    @Mock
    TrainingRepository trainings;
    @Mock
    UserRepository users;
    @Mock
    TrainingTypeRepository types;
    @Mock
    com.training.gym.util.UsernameGenerator usernameGenerator;
    @Mock
    com.training.gym.util.PasswordGenerator passwordGenerator;
    @Mock
    org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Mock
    CredentialService credentials;
    @InjectMocks
    TrainerServiceImpl service;

    private TrainingType fitness() {
        return TrainingType.builder().id(1L).name("Fitness").build();
    }

    private Trainer sara() {
        return Trainer.builder().id(2L).user(User.builder().id(2L).firstName("Sara").lastName("Connor").userName("sara.connor").password("hash").active(true).build()).specialization(fitness()).build();
    }

    @Test
    void shouldRegisterTrainerAndResolveSpecialization() {
        // Arrange
        Trainer sara = sara();
        TrainingType type = fitness();
        when(usernameGenerator.generate(eq("Sara"), eq("Connor"), any())).thenReturn("sara.connor");
        when(passwordGenerator.generate()).thenReturn("raw-secret");
        when(encoder.encode("raw-secret")).thenReturn("encoded");
        when(types.findById(1L)).thenReturn(Optional.of(type));
        when(trainers.save(sara)).thenReturn(sara);
        // Act
        RegistrationResult<Trainer> result = service.registerTrainer(sara);
        // Assert
        assertEquals("raw-secret", result.initialPassword());
        assertEquals("encoded", sara.getUser().getPassword());
        assertSame(type, sara.getSpecialization());
    }

    @Test
    void shouldRejectUnknownSpecializationDuringRegistration() {
        // Arrange
        Trainer sara = sara();
        when(usernameGenerator.generate(eq("Sara"), eq("Connor"), any())).thenReturn("sara.connor");
        when(passwordGenerator.generate()).thenReturn("secret");
        when(types.findById(1L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.registerTrainer(sara));
        verify(trainers, never()).save(any());
    }

    @Test
    void shouldUpdateNamesAndSpecializationWithoutChangingPassword() {
        // Arrange
        Trainer existing = sara();
        Trainer incoming = sara();
        incoming.getUser().setFirstName("Jym");
        incoming.getUser().setLastName("Kerry");
        incoming.getUser().setPassword(null);
        incoming.setSpecialization(TrainingType.builder().id(3L).build());
        TrainingType yoga = TrainingType.builder().id(3L).name("Yoga").build();
        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.of(existing));
        when(types.findById(3L)).thenReturn(Optional.of(yoga));
        // Act
        Trainer result = service.updateTrainer(incoming);
        // Assert
        assertSame(existing, result);
        assertEquals("Jym", result.getUser().getFirstName());
        assertEquals("Kerry", result.getUser().getLastName());
        assertEquals("hash", result.getUser().getPassword());
        assertSame(yoga, result.getSpecialization());
    }

    @Test
    void shouldRejectUnknownTrainerUpdate() {
        // Arrange
        Trainer incoming = sara();
        when(trainers.findByUserName("sara.connor")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(incoming));
    }

    @Test
    void shouldRetrieveTrainerById() {
        // Arrange
        Trainer sara = sara();
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        // Act
        Trainer result = service.getTrainerById(2L);
        // Assert
        assertSame(sara, result);
    }

    @Test
    void shouldRejectUnknownTrainerId() {
        // Arrange
        when(trainers.findById(404L)).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.getTrainerById(404L));
    }

    @Test
    void shouldRejectRepeatedActivation() {
        // Arrange
        Trainer sara = sara();
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        // Act / Assert
        assertThrows(IllegalStateException.class, () -> service.activateTrainer(sara));
        verify(trainers, never()).activateTrainer(any());
    }

    @Test
    void shouldActivateInactiveTrainer() {
        // Arrange
        Trainer sara = sara();
        sara.getUser().setActive(false);
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        // Act
        service.activateTrainer(sara);
        // Assert
        verify(trainers).activateTrainer(sara);
    }

    @Test
    void shouldDeactivateActiveTrainer() {
        // Arrange
        Trainer sara = sara();
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        // Act
        service.deactivateTrainer(sara);
        // Assert
        verify(trainers).deactivateTrainer(sara);
    }

    @Test
    void shouldRejectRepeatedDeactivation() {
        // Arrange
        Trainer sara = sara();
        sara.getUser().setActive(false);
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        // Act / Assert
        assertThrows(IllegalStateException.class, () -> service.deactivateTrainer(sara));
        verify(trainers, never()).deactivateTrainer(any());
    }

    @Test
    void shouldFindUnassignedTrainers() {
        // Arrange
        Trainer sara = sara();
        when(trainers.findUnassignedTrainers("james.bond")).thenReturn(List.of(sara));
        // Act
        List<Trainer> result = service.getUnassignedTrainers("james.bond");
        // Assert
        assertEquals(List.of(sara), result);
    }

    @Test
    void shouldDelegateTrainingSearch() {
        // Arrange
        var criteria = new com.training.gym.dto.trainer.TrainerTrainingCriteria(null, null, null);
        when(trainings.findByTrainerUsername("sara.connor", criteria)).thenReturn(List.of());
        // Act
        List<Training> result = service.findTrainings("sara.connor", criteria);
        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDelegatePasswordChange() {
        // Arrange
        String username = "sara.connor";
        // Act
        service.changePassword(username, "old", "new");
        // Assert
        verify(credentials).changePassword(username, "old", "new");
    }
}
