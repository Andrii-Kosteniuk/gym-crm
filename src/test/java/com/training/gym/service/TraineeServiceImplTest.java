//package com.training.gym.service;
//
//import com.training.gym.domain.criteria.TraineeTrainingFilter;
//import com.training.gym.domain.mapper.TrainingCriteriaMapper;
//import com.training.gym.domain.model.Trainee;
//import com.training.gym.domain.model.Training;
//import com.training.gym.domain.model.User;
//import com.training.gym.dto.crireria.TraineeTrainingCriteria;
//import com.training.gym.exception.BusinessRuleViolationException;
//import com.training.gym.exception.EntityNotFoundException;
//import com.training.gym.exception.IncorrectMethodArgumentException;
//import com.training.gym.persistence.entity.TraineeEntity;
//import com.training.gym.persistence.entity.TrainerEntity;
//import com.training.gym.persistence.entity.TrainingEntity;
//import com.training.gym.persistence.entity.UserEntity;
//import com.training.gym.repository.TraineeRepository;
//import com.training.gym.repository.TrainerRepository;
//import com.training.gym.repository.TrainingRepository;
//import com.training.gym.repository.UserRepository;
//import com.training.gym.service.auth.CredentialService;
//import com.training.gym.service.mapper.TraineePersistenceMapper;
//import com.training.gym.service.mapper.TrainingPersistenceMapper;
//import com.training.gym.service.trainee.TraineeServiceImpl;
//import com.training.gym.util.PasswordGenerator;
//import com.training.gym.util.UsernameGenerator;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TraineeServiceImplTest {
//    @Mock
//    TraineeRepository traineeRepository;
//    @Mock
//    TrainingRepository trainingRepository;
//    @Mock
//    TrainerRepository trainerRepository;
//    @Mock
//    UserRepository userRepository;
//    @Mock
//    UsernameGenerator usernameGenerator;
//    @Mock
//    PasswordGenerator passwordGenerator;
//    @Mock
//    PasswordEncoder passwordEncoder;
//    @Mock
//    CredentialService credentialService;
//    @Mock
//    TrainingCriteriaMapper criteriaMapper;
//    @Mock
//    TraineePersistenceMapper traineeMapper;
//    @Mock
//    TrainingPersistenceMapper trainingMapper;
//    @InjectMocks
//    TraineeServiceImpl service;
//
//    private Trainee domaintrainee() {
//        return Trainee.builder().id(1L)
//                .user(User.builder()
//                        .firstName("James")
//                        .lastName("Bond")
//                        .userName("james.bond")
//                        .build())
//                .build();
//    }
//
//    private TraineeEntity entity(boolean active) {
//        UserEntity userEntity = new UserEntity();
//        userEntity.setActive(active);
//        userEntity.setFirstName("James");
//        userEntity.setLastName("Bond");
//        TraineeEntity traineeEntity = new TraineeEntity();
//        traineeEntity.setId(1L);
//        traineeEntity.setUserEntity(userEntity);
//        return traineeEntity;
//    }
//
//    @Test
//    void registrationMapsDomainToEntityAndBack() {
//        Trainee trainee = domaintrainee();
//        TraineeEntity stored = entity(true);
//        Trainee domainTrainee = domaintrainee();
//        when(usernameGenerator.generate(eq("James"), eq("Bond"), any())).thenReturn("james.bond");
//        when(passwordGenerator.generate()).thenReturn("initial-secret");
//        when(passwordEncoder.encode("initial-secret")).thenReturn("encoded");
//        when(traineeMapper.toEntity(trainee)).thenReturn(stored);
//        when(traineeRepository.save(stored)).thenReturn(stored);
//        when(traineeMapper.toTraineeDomain(stored)).thenReturn(domainTrainee);
//        var result = service.registerTrainee(trainee);
//        assertSame(domainTrainee, result.profile());
//        assertEquals("initial-secret", result.initialPassword());
//        assertEquals("encoded", trainee.getUser().getPassword());
//        assertTrue(trainee.getUser().getActive());
//        verify(traineeRepository).save(stored);
//    }
//
//    @Test
//    void registrationRejectsInvalidInput() {
//        assertThrows(NullPointerException.class, () -> service.registerTrainee(null));
//        assertThrows(NullPointerException.class, () -> service.registerTrainee(Trainee.builder().build()));
//        verifyNoInteractions(traineeRepository);
//    }
//
//    @Test
//    void updateModifiesManagedEntityAndMapsResponse() {
//        Trainee trainee = domaintrainee();
//        TraineeEntity existing = entity(true);
//        Trainee resultTrainee = domaintrainee();
//        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(existing));
//        when(traineeRepository.update(existing)).thenReturn(existing);
//        when(traineeMapper.toTraineeDomain(existing)).thenReturn(resultTrainee);
//        assertSame(resultTrainee, service.updateTrainee(trainee));
//        verify(traineeRepository).update(existing);
//        assertEquals("James", existing.getUserEntity().getFirstName());
//    }
//
//    @Test
//    void updateRejectsMissingProfileAndNullInputs() {
//        assertThrows(NullPointerException.class, () -> service.updateTrainee(null));
//        assertThrows(NullPointerException.class, () -> service.updateTrainee(Trainee.builder().build()));
//        assertThrows(EntityNotFoundException.class, () -> service.updateTrainee(domaintrainee()));
//        verify(traineeRepository, never()).update(any());
//    }
//
//    @Test
//    void findByIdAndUsernameMapPersistedEntities() {
//        TraineeEntity traineeEntity = entity(true);
//        Trainee resultTrainee = domaintrainee();
//        when(traineeRepository.findById(1L)).thenReturn(Optional.of(traineeEntity));
//        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(traineeEntity));
//        when(traineeMapper.toTraineeDomain(traineeEntity)).thenReturn(resultTrainee);
//        assertSame(resultTrainee, service.getTraineeById(1L));
//        assertSame(resultTrainee, service.findByUserName("james.bond"));
//        verify(traineeMapper, times(2)).toTraineeDomain(traineeEntity);
//    }
//
//    @Test
//    void findRejectsMissingAndNullIdentifiers() {
//        assertThrows(NullPointerException.class, () -> service.getTraineeById(null));
//        assertThrows(NullPointerException.class, () -> service.findByUserName(null));
//        assertThrows(EntityNotFoundException.class, () -> service.getTraineeById(10L));
//        assertThrows(EntityNotFoundException.class, () -> service.findByUserName("unknown"));
//    }
//
//    @Test
//    void deleteUsesPersistenceEntities() {
//        TraineeEntity traineeEntity = entity(true);
//        when(traineeRepository.findByUserName("james.bond")).thenReturn(Optional.of(traineeEntity));
//        service.removeTraineeByUsername("james.bond");
//        verify(traineeRepository).delete(traineeEntity);
//        verify(userRepository).delete(traineeEntity.getUserEntity());
//        assertThrows(EntityNotFoundException.class, () -> service.removeTraineeByUsername("unknown"));
//    }
//
//    @Test
//    void activationChecksExistingState() {
//        Trainee domaintrainee = domaintrainee();
//        TraineeEntity inactive = entity(false);
//        TraineeEntity active = entity(true);
//        when(traineeRepository.findById(1L)).thenReturn(Optional.of(inactive), Optional.of(active), Optional.of(active), Optional.of(inactive));
//        when(traineeMapper.toEntity(domaintrainee)).thenReturn(inactive, active);
//        service.activateTrainee(domaintrainee);
//        assertThrows(IllegalStateException.class, () -> service.activateTrainee(domaintrainee));
//        service.deactivateTrainee(domaintrainee);
//        assertThrows(IllegalStateException.class, () -> service.deactivateTrainee(domaintrainee));
//        verify(traineeRepository).activateTrainee(inactive);
//        verify(traineeRepository).deactivateTrainee(active);
//    }
//
//    @Test
//    void activationRejectsMissingProfiles() {
//        assertThrows(NullPointerException.class, () -> service.activateTrainee(null));
//        assertThrows(NullPointerException.class, () -> service.deactivateTrainee(null));
//        assertThrows(EntityNotFoundException.class, () -> service.activateTrainee(domaintrainee()));
//        assertThrows(EntityNotFoundException.class, () -> service.deactivateTrainee(domaintrainee()));
//    }
//
//    @Test
//    void trainingSearchMapsEachEntity() {
//        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(null, null, null, null, "");
//        TraineeTrainingFilter filter = new TraineeTrainingFilter(null, null, null, null);
//        TrainingEntity trainingEntity = new TrainingEntity();
//        Training training = Training.builder().build();
//        when(criteriaMapper.toFilter(criteria)).thenReturn(filter);
//        when(trainingRepository.findByTraineeUsername("james.bond", filter)).thenReturn(List.of(trainingEntity));
//        when(trainingMapper.toTrainingDomain(trainingEntity)).thenReturn(training);
//        assertEquals(List.of(training), service.findTrainings(" james.bond ", criteria));
//        assertThrows(IncorrectMethodArgumentException.class, () -> service.findTrainings(" ", criteria));
//        assertThrows(NullPointerException.class, () -> service.findTrainings("james.bond", null));
//    }
//
//    @Test
//    void replaceTrainersChecksBothParticipants() {
//        TraineeEntity traineeEntity = entity(true);
//        TrainerEntity trainerEntity = new TrainerEntity();
//        UserEntity userEntity = new UserEntity();
//        userEntity.setActive(true);
//        trainerEntity.setUserEntity(userEntity);
//        when(traineeRepository.findById(1L)).thenReturn(Optional.of(traineeEntity));
//        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainerEntity));
//        service.replaceTrainers("sarah.connor", Set.of(2L));
//        verify(traineeRepository).replaceTrainers("sarah.connor", Set.of(2L));
//    }
//
//    @Test
//    void replaceTrainersRejectsInvalidAndInactiveParticipants() {
//        assertThrows(NullPointerException.class, () -> service.replaceTrainers(null, Set.of()));
//        assertThrows(NullPointerException.class, () -> service.replaceTrainers("sarah.connor", null));
//        Set<Long> withNull = new HashSet<>();
//        withNull.add(null);
//        assertThrows(IncorrectMethodArgumentException.class, () -> service.replaceTrainers("sarah.connor", withNull));
//        assertThrows(EntityNotFoundException.class, () -> service.replaceTrainers("sarah.connor", Set.of()));
//        when(traineeRepository.findById(1L)).thenReturn(Optional.of(entity(false)), Optional.of(entity(true)), Optional.of(entity(true)));
//        assertThrows(BusinessRuleViolationException.class, () -> service.replaceTrainers("sarah.connor", Set.of()));
//        assertThrows(EntityNotFoundException.class, () -> service.replaceTrainers("sarah.connor", Set.of(2L)));
//        TrainerEntity inactive = new TrainerEntity();
//        UserEntity user = new UserEntity();
//        user.setActive(false);
//        inactive.setUserEntity(user);
//        when(trainerRepository.findById(2L)).thenReturn(Optional.of(inactive));
//        assertThrows(BusinessRuleViolationException.class, () -> service.replaceTrainers("sarah.connor", Set.of(2L)));
//        verify(traineeRepository, never()).replaceTrainers(anyString(), anySet());
//    }
//
//    @Test
//    void changePasswordDelegates() {
//        service.changePassword("james.bond", "old", "new");
//        verify(credentialService).changePassword("james.bond", "old", "new");
//    }
//}
