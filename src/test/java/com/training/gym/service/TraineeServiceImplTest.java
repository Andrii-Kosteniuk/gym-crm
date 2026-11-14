package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    TraineeRepository trainees;
    @Mock
    TrainerRepository trainers;
    @Mock
    TrainingRepository trainings;
    @Mock
    UserRepository users;
    @Mock
    com.training.gym.util.UsernameGenerator usernameGenerator;
    @Mock
    com.training.gym.util.PasswordGenerator passwordGenerator;
    @Mock
    org.springframework.security.crypto.password.PasswordEncoder encoder;
    @Mock
    CredentialService credentials;
    @InjectMocks
    TraineeServiceImpl service;

    private Trainee james() {
        return Trainee.builder().id(1L).user(User.builder().id(1L).firstName("James").lastName("Bond").userName("james.bond").password("hash").active(true).build()).dateOfBirth(LocalDate.of(1990, 4, 13)).build();
    }

    @Test
    void shouldRegisterTraineeWithHashedPassword() {
        // Arrange
        Trainee james = james();
        when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn("james.bond");
        when(passwordGenerator.generate()).thenReturn("raw-secret");
        when(encoder.encode("raw-secret")).thenReturn("hash-new");
        when(trainees.save(james)).thenReturn(james);
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
        verifyNoInteractions(trainees);
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
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(existing));
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
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainee(input));
    }

    @Test
    void shouldRetrieveTraineeById() {
        // Arrange
        Trainee james = james();
        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        // Act
        Trainee result = service.getTraineeById(1L);
        // Assert
        assertSame(james, result);
    }

    @Test
    void shouldRejectUnknownUsername() {
        // Arrange
        when(trainees.findByUserName("jym.kerry")).thenReturn(Optional.empty());
        // Act / Assert
        assertThrows(EntityNotFoundException.class, () -> service.findByUserName("jym.kerry"));
    }

    @Test
    void shouldActivateInactiveTrainee() {
        // Arrange
        Trainee james = james();
        james.getUser().setActive(false);
        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        // Act
        service.activateTrainee(james);
        // Assert
        assertTrue(james.getUser().getActive());
    }

    @Test
    void shouldRejectRepeatedActivation() {
        // Arrange
        Trainee james = james();
        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        // Act / Assert
        assertThrows(IllegalStateException.class, () -> service.activateTrainee(james));
    }

    @Test
    void shouldDeactivateActiveTrainee() {
        // Arrange
        Trainee james = james();
        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        // Act
        service.deactivateTrainee(james);
        // Assert
        assertFalse(james.getUser().getActive());
    }

    @Test
    void shouldRejectRepeatedDeactivation() {
        // Arrange
        Trainee james = james();
        james.getUser().setActive(false);
        when(trainees.findById(1L)).thenReturn(Optional.of(james));
        // Act / Assert
        assertThrows(IllegalStateException.class, () -> service.deactivateTrainee(james));
    }

    @Test
    void shouldDeleteTraineeAndUser() {
        // Arrange
        Trainee james = james();
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(james));
        // Act
        service.removeTraineeByUsername("james.bond");
        // Assert
        var order = inOrder(trainees, users);
        order.verify(trainees).delete(james);
        order.verify(users).delete(james.getUser());
    }

    @Test
    void shouldRejectInvalidAssignmentIds() {
        // Arrange
        Set<Long> invalid = new HashSet<>();
        invalid.add(null);
        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.replaceTrainers("james.bond", invalid));
        verifyNoInteractions(trainers);
    }

    @Test
    void shouldReplaceAssignments() {
        // Arrange
        Trainee james = james();
        Trainer sara = Trainer.builder().id(2L).user(User.builder().firstName("Sara").lastName("Connor").build()).build();
        when(trainers.findById(2L)).thenReturn(Optional.of(sara));
        when(trainees.findByUserName("james.bond")).thenReturn(Optional.of(james));
        // Act
        Set<Trainer> result = service.replaceTrainers("james.bond", Set.of(2L));
        // Assert
        assertEquals(Set.of(sara), result);
        assertTrue(sara.getTrainees().contains(james));
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

    @Test
    void shouldDelegateTrainingCriteria() {
        // Arrange
        var criteria = new com.training.gym.dto.trainee.TraineeTrainingCriteria(null, null, null, null);
        when(trainings.findByTraineeUsername("james.bond", criteria)).thenReturn(List.of());
        // Act
        List<Training> result = service.findTrainings("james.bond", criteria);
        // Assert
        assertTrue(result.isEmpty());
        verify(trainings).findByTraineeUsername("james.bond", criteria);
    }
}
