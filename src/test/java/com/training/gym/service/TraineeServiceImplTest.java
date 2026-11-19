package com.training.gym.service;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.dto.auth.RegistrationResult;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.UserRepository;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.User;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    UsernameGenerator usernameGenerator;
    @Mock
    PasswordGenerator passwordGenerator;
    @Mock
    TraineeTrainingFilter filter;
    @Mock
    PasswordEncoder encoder;
    @Mock
    CredentialService credentials;
    @InjectMocks
    TraineeServiceImpl service;

    private Trainee james() {
        return Trainee.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .firstName("James")
                        .lastName("Bond")
                        .userName("james.bond")
                        .password("hash")
                        .active(true)
                        .build())
                .dateOfBirth(LocalDate.of(1990, 4, 13))
                .build();
    }

    @Test
    void shouldRegisterTraineeWithHashedPassword() {
        // Arrange
        Trainee james = james();
        when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn("james.bond");
        when(passwordGenerator.generate()).thenReturn("raw-secret");
        when(encoder.encode("raw-secret")).thenReturn("hash-new");
        when(traineeRepository.save(james)).thenReturn(james);
        // Act
        RegistrationResult<Trainee> result = service.registerTrainee(james);
        // Assert
        assertSame(james, result.profile());
        assertEquals("raw-secret", result.initialPassword());
        assertEquals("hash-new", james.getUser().getPassword());
        assertTrue(james.getUser().getActive());
    }

    @Test
    void shouldRejectRegistrationWithoutUser() {
        // Arrange
        Trainee invalid = Trainee.builder().build();
        // Act / Assert
        assertThrows(NullPointerException.class, () -> service.registerTrainee(invalid));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void shouldUpdateOnlyEditableFields() {
        // Arrange
        Trainee existing = james();
        Trainee input = james();
        input.getUser().setFirstName("Jym");
        input.getUser().setLastName("Kerry");
        input.getUser().setPassword(null);
        input.getUser().setActive(false);
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(existing));
        when(traineeRepository.update(existing)).thenReturn(existing);
        // Act
        Trainee result = service.updateTrainee(input);
        // Assert
        assertSame(existing, result);
        assertEquals("Jym", existing.getUser().getFirstName());
        assertEquals("Kerry", existing.getUser().getLastName());
        assertEquals("hash", existing.getUser().getPassword());
        assertTrue(existing.getUser().getActive());
    }

    @Test
    void shouldRejectUpdatingUnknownTrainee() {
        // Arrange
        Trainee input = james();
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainee(input));
    }

    @Test
    void shouldRetrieveTraineeById() {
        // Arrange
        Trainee james = james();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(james));
        // Act
        Trainee result = service.getTraineeById(1L);
        // Assert
        assertSame(james, result);
    }

    @Test
    void shouldRejectUnknownUsername() {
        // Arrange
        when(traineeRepository.findByUserName("jym.kerry")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.findByUserName("jym.kerry"));
    }

    @Test
    void shouldActivateInactiveTrainee() {
        // Arrange
        Trainee james = james();
        james.getUser().setActive(false);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(james));
        // Act
        service.activateTrainee(james);
        // Assert
        verify(traineeRepository).activateTrainee(james);
    }

    @Test
    void shouldRejectRepeatedActivation() {
        // Arrange
        Trainee james = james();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(james));
        // Act / Assert
        assertThrows(IllegalStateException.class, () -> service.activateTrainee(james));
    }

    @Test
    void shouldDeactivateActiveTrainee() {
        // Arrange
        Trainee james = james();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(james));
        // Act
        service.deactivateTrainee(james);
        // Assert
        verify(traineeRepository).deactivateTrainee(james);
    }

    @Test
    void shouldRejectRepeatedDeactivation() {
        // Arrange
        Trainee james = james();
        james.getUser().setActive(false);
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(james));
        // Act / Assert
        assertThrows(IllegalStateException.class, () -> service.deactivateTrainee(james));
    }

    @Test
    void shouldDeleteTraineeAndUser() {
        // Arrange
        Trainee james = james();
        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(james));
        // Act
        service.removeTraineeByUsername("james.bond");
        // Assert
        var order = inOrder(traineeRepository, userRepository);
        order.verify(traineeRepository).delete(james);
        order.verify(userRepository).delete(james.getUser());
    }

    @Test
    void shouldRejectInvalidAssignmentIds() {
        // Arrange
        Set<Long> invalid = new HashSet<>();
        invalid.add(null);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.replaceTrainers(1L, invalid));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldReplaceAssignments() {
        // Arrange
        Long traineeId = 1L;
        Long trainerId = 2L;
        Set<Long> trainerIds = Set.of(trainerId);

        Trainee james = james();

        Trainer sara = Trainer.builder()
                .id(trainerId)
                .user(User.builder()
                        .firstName("Sara")
                        .lastName("Connor")
                        .active(true)
                        .build())
                .build();

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(james));

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(sara));

        // Act
        service.replaceTrainers(traineeId, trainerIds);

        // Assert
        verify(traineeRepository).findById(traineeId);
        verify(trainerRepository).findById(trainerId);
        verify(traineeRepository).replaceTrainers(traineeId, trainerIds);
    }

    @Test
    void shouldDelegatePasswordChange() {
        // Arrange
        String username = "james.bond";
        // Act
        service.changePassword(username, "old", "new");
        // Assert
        verify(credentials).changePassword(username, "old", "new");
    }

}
