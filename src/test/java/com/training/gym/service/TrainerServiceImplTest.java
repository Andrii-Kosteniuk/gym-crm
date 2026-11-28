package com.training.gym.service;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.User;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.exception.UserNotFoundException;
import com.training.gym.monitoring.metrics.RegistrationMetrics;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.auth.CredentialService;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainingPersistenceMapper;
import com.training.gym.service.trainee.TraineeServiceImpl;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    private static final String USERNAME = "james.bond";

    @Mock
    TraineeRepository traineeRepository;
    @Mock
    TrainingRepository trainingRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    UsernameGenerator usernameGenerator;
    @Mock
    PasswordGenerator passwordGenerator;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CredentialService credentialService;
    @Mock
    TrainingCriteriaMapper criteriaMapper;
    @Mock
    TraineePersistenceMapper traineeMapper;
    @Mock
    TrainingPersistenceMapper trainingMapper;
    @Mock
    RegistrationMetrics metrics;

    @InjectMocks
    TraineeServiceImpl service;

    private Trainee trainee() {
        return Trainee.builder()
                .id(1L)
                .user(User.builder()
                        .firstName("James")
                        .lastName("Bond")
                        .userName(USERNAME)
                        .build())
                .build();
    }

    private TraineeEntity entity() {
        var user = new UserEntity();
        user.setActive(true);
        user.setFirstName("James");
        user.setLastName("Bond");
        var entity = new TraineeEntity();
        entity.setId(1L);
        entity.setUserEntity(user);
        return entity;
    }

    @Test
    void registersTrainee() {
        // Arrange
        var trainee = trainee();
        var entity = entity();
        var saved = trainee();
        when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn(USERNAME);
        when(passwordGenerator.generate()).thenReturn("initial-secret");
        when(passwordEncoder.encode("initial-secret")).thenReturn("encoded");
        when(traineeMapper.toEntity(trainee)).thenReturn(entity);
        doNothing().when(metrics).recordTraineeRegistration();
        when(traineeRepository.save(entity)).thenReturn(entity);
        when(traineeMapper.toTraineeDomain(entity)).thenReturn(saved);

        // Act
        var result = service.registerTrainee(trainee);

        // Assert
        assertSame(saved, result.profile());
        assertEquals("initial-secret", result.initialPassword());
        assertEquals("encoded", trainee.getUser().getPassword());
        assertTrue(trainee.getUser().getActive());
        verify(traineeRepository).save(entity);
    }

    @Test
    void rejectsNullRegistration() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.registerTrainee(null));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void rejectsRegistrationWithoutUser() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> service.registerTrainee(Trainee.builder().build()));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void updatesTrainee() {
        // Arrange
        var trainee = trainee();
        var entity = entity();
        var updated = trainee();
        when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity));
        when(traineeRepository.update(entity)).thenReturn(entity);
        when(traineeMapper.toTraineeDomain(entity)).thenReturn(updated);

        // Act
        var result = service.updateTrainee(trainee);

        // Assert
        assertSame(updated, result);
        assertEquals("James", entity.getUserEntity().getFirstName());
        verify(traineeRepository).update(entity);
    }

    @Test
    void rejectsNullUpdate() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.updateTrainee(null));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void rejectsUpdateWithoutUser() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> service.updateTrainee(Trainee.builder().build()));
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void rejectsUpdateOfMissingTrainee() {
        // Arrange
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> service.updateTrainee(trainee()));
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void findsTraineeById() {
        // Arrange
        var entity = entity();
        var trainee = trainee();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(traineeMapper.toTraineeDomain(entity)).thenReturn(trainee);

        // Act
        var result = service.getTraineeById(1L);

        // Assert
        assertSame(trainee, result);
    }

    @Test
    void findsTraineeByUsername() {
        // Arrange
        var entity = entity();
        var trainee = trainee();
        when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity));
        when(traineeMapper.toTraineeDomain(entity)).thenReturn(trainee);

        // Act
        var result = service.findByUserName(USERNAME);

        // Assert
        assertSame(trainee, result);
    }

    @Test
    void rejectsNullLookupIdentifiers() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.getTraineeById(null));
        assertThrows(NullPointerException.class, () -> service.findByUserName(null));
    }

    @Test
    void rejectsMissingTraineeLookup() {
        // Arrange
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> service.getTraineeById(10L));
        assertThrows(UserNotFoundException.class, () -> service.findByUserName("unknown"));
    }

    @Test
    void deletesTrainee() {
        // Arrange
        var entity = entity();
        when(traineeRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity));

        // Act
        service.removeTraineeByUsername(USERNAME);

        // Assert
        verify(traineeRepository).delete(entity);
        verify(userRepository).delete(entity.getUserEntity());
    }

    @Test
    void rejectsDeletionOfMissingTrainee() {
        // Arrange
        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> service.removeTraineeByUsername("unknown"));
        verify(traineeRepository, never()).delete(any());
    }

    @Test
    void rejectsNullStatusUsername() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.changeProfileStatus(null, true));
    }

    @Test
    void rejectsStatusChangeOfMissingTrainee() {
        // Arrange
        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> service.changeProfileStatus(USERNAME, true));
    }

    @Test
    void searchesTrainings() {
        // Arrange
        var criteria = new TraineeTrainingCriteria(null, null, null, null, "");
        var filter = new TraineeTrainingFilter(null, null, null, null);
        var entity = new TrainingEntity();
        var training = Training.builder().build();
        when(criteriaMapper.toFilter(criteria)).thenReturn(filter);
        when(trainingRepository.findByTraineeUsername(USERNAME, filter)).thenReturn(List.of(entity));
        when(trainingMapper.toTrainingDomain(entity)).thenReturn(training);

        // Act
        var result = service.findTrainings(" james.bond ", criteria);

        // Assert
        assertEquals(List.of(training), result);
    }

    @Test
    void rejectsInvalidTrainingSearch() {
        // Arrange
        var criteria = new TraineeTrainingCriteria(null, null, null, null, "");

        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class,
                () -> service.findTrainings(" ", criteria));
        assertThrows(NullPointerException.class,
                () -> service.findTrainings(USERNAME, null));
    }

    @Test
    void rejectsNullTrainerReplacementArguments() {
        // Arrange
        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class, () -> service.replaceTrainersByUsername(null, Set.of()));
        assertThrows(IncorrectMethodArgumentException.class, () -> service.replaceTrainersByUsername(USERNAME, null));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void rejectsNullTrainerId() {
        // Arrange
        Set<String> usernames = new HashSet<>();
        usernames.add(null);

        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class,
                () -> service.replaceTrainersByUsername(USERNAME, usernames));
        verify(traineeRepository, never()).replaceTrainers(anyString(), anySet());
    }

    @Test
    void rejectsMissingTraineeWhenReplacingTrainers() {
        // Arrange
        // Act & Assert
        assertThrows(IncorrectMethodArgumentException.class,
                () -> service.replaceTrainersByUsername(USERNAME, Set.of()));
        verify(traineeRepository, never()).replaceTrainers(anyString(), anySet());
    }

}
