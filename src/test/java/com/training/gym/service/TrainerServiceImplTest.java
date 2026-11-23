package com.training.gym.service;

import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.domain.model.User;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.TrainingTypeRepository;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.auth.CredentialService;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import com.training.gym.service.mapper.TrainingPersistenceMapper;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import com.training.gym.service.trainer.TrainerServiceImpl;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    private static final String USERNAME = "sara.connor";
    private static final Long TRAINER_ID = 2L;
    private static final Long TYPE_ID = 3L;

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
    TrainingTypeRepository trainingTypeRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CredentialService credentialService;
    @Mock
    TrainingCriteriaMapper criteriaMapper;
    @Mock
    TrainerPersistenceMapper trainerMapper;
    @Mock
    TrainingPersistenceMapper trainingMapper;
    @Mock
    TrainingTypePersistenceMapper trainingTypeMapper;
    @InjectMocks
    TrainerServiceImpl service;

    private Trainer trainer() {
        return Trainer.builder()
                .id(TRAINER_ID)
                .user(User.builder()
                        .firstName("Sara")
                        .lastName("Connor")
                        .userName(USERNAME)
                        .build())
                .specialization(TrainingType.builder().id(TYPE_ID).build())
                .build();
    }

    private TrainerEntity entity(boolean active) {
        UserEntity user = new UserEntity();
        user.setFirstName("Sara");
        user.setLastName("Connor");
        user.setUserName(USERNAME);
        user.setActive(active);
        TrainerEntity entity = new TrainerEntity();
        entity.setId(TRAINER_ID);
        entity.setUserEntity(user);
        return entity;
    }

    private TrainingTypeEntity specialization() {
        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(TYPE_ID);
        return type;
    }

    @Test
    void registersTrainer() {
        Trainer input = trainer();
        TrainerEntity saved = entity(true);
        Trainer output = trainer();
        TrainingTypeEntity type = specialization();
        TrainingType domainType = TrainingType.builder().id(TYPE_ID).name("Fitness").build();
        when(usernameGenerator.generate(eq("Sara"), eq("Connor"), any())).thenReturn(USERNAME);
        when(passwordGenerator.generate()).thenReturn("secret");
        when(trainingTypeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(trainingTypeMapper.toTrainingTypeDomain(type)).thenReturn(domainType);
        when(trainerMapper.toEntity(input)).thenReturn(saved);
        when(trainerRepository.save(saved)).thenReturn(saved);
        when(trainerMapper.toTrainerDomain(saved)).thenReturn(output);

        var result = service.registerTrainer(input);

        assertSame(output, result.profile());
        assertEquals("secret", result.initialPassword());
        assertEquals(USERNAME, input.getUser().getUserName());
        assertEquals("hash", input.getUser().getPassword());
        assertTrue(input.getUser().getActive());
        assertSame(domainType, input.getSpecialization());
        verify(trainerRepository).save(saved);
    }

    @Test
    void rejectsNullRegistration() {
        assertThrows(NullPointerException.class, () -> service.registerTrainer(null));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsRegistrationWithoutUser() {
        Trainer input = Trainer.builder().build();

        assertThrows(NullPointerException.class, () -> service.registerTrainer(input));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsRegistrationWithoutSpecialization() {
        Trainer input = Trainer.builder().user(User.builder().build()).build();

        assertThrows(NullPointerException.class, () -> service.registerTrainer(input));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsUnknownRegistrationSpecialization() {
        Trainer input = trainer();
        when(usernameGenerator.generate(eq("Sara"), eq("Connor"), any())).thenReturn(USERNAME);
        when(passwordGenerator.generate()).thenReturn("secret");

        assertThrows(EntityNotFoundException.class, () -> service.registerTrainer(input));

        verify(trainingTypeRepository).findById(TYPE_ID);
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void updatesManagedTrainer() {
        Trainer input = trainer();
        input.getUser().setFirstName("Sarah");
        input.getUser().setLastName("Updated");
        TrainerEntity managed = entity(true);
        managed.getUserEntity().setPassword("existing-hash");

        Trainer output = trainer();
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(managed));
        when(trainerRepository.update(managed)).thenReturn(managed);
        when(trainerMapper.toTrainerDomain(managed)).thenReturn(output);

        Trainer result = service.updateTrainer(input);

        assertSame(output, result);
        assertEquals("Sarah", managed.getUserEntity().getFirstName());
        assertEquals("Updated", managed.getUserEntity().getLastName());
        assertEquals(USERNAME, managed.getUserEntity().getUserName());
        assertEquals("existing-hash", managed.getUserEntity().getPassword());
        assertTrue(managed.getUserEntity().getActive());
        verify(trainerRepository).update(managed);
    }

    @Test
    void rejectsNullUpdate() {
        assertThrows(NullPointerException.class, () -> service.updateTrainer(null));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsUpdateWithoutUser() {
        Trainer input = Trainer.builder().build();

        assertThrows(NullPointerException.class, () -> service.updateTrainer(input));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsMissingTrainerUpdate() {
        Trainer input = trainer();

        assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(input));

        verify(trainerRepository, never()).update(any());
    }


    @Test
    void findsTrainerById() {
        TrainerEntity stored = entity(true);
        Trainer expected = trainer();
        when(trainerRepository.findById(TRAINER_ID)).thenReturn(Optional.of(stored));
        when(trainerMapper.toTrainerDomain(stored)).thenReturn(expected);

        Trainer result = service.getTrainerById(TRAINER_ID);

        assertSame(expected, result);
    }

    @Test
    void rejectsNullTrainerId() {
        assertThrows(NullPointerException.class, () -> service.getTrainerById(null));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsUnknownTrainerId() {
        assertThrows(EntityNotFoundException.class, () -> service.getTrainerById(TRAINER_ID));

        verify(trainerRepository).findById(TRAINER_ID);
    }

    @Test
    void findsTrainerByUsername() {
        TrainerEntity stored = entity(true);
        Trainer expected = trainer();
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(stored));
        when(trainerMapper.toTrainerDomain(stored)).thenReturn(expected);

        Trainer result = service.findByUserName(USERNAME);

        assertSame(expected, result);
    }

    @Test
    void rejectsNullTrainerUsername() {
        assertThrows(NullPointerException.class, () -> service.findByUserName(null));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsUnknownTrainerUsername() {
        assertThrows(EntityNotFoundException.class, () -> service.findByUserName(USERNAME));

        verify(trainerRepository).findByUserName(USERNAME);
    }

    @Test
    void activatesInactiveTrainer() {
        TrainerEntity stored = entity(false);
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(stored));

        service.changeProfileStatus(USERNAME, true);

        verify(trainerRepository).changeProfileStatus(stored, true);
    }

    @Test
    void deactivatesActiveTrainer() {
        TrainerEntity stored = entity(true);
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(stored));

        service.changeProfileStatus(USERNAME, false);

        verify(trainerRepository).changeProfileStatus(stored, false);
    }

    @Test
    void rejectsActivationOfActiveTrainer() {
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity(true)));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.changeProfileStatus(USERNAME, true));

        assertEquals("Trainer profile is already active.", exception.getMessage());
        verify(trainerRepository, never()).changeProfileStatus(any(), anyBoolean());
    }

    @Test
    void rejectsDeactivationOfInactiveTrainer() {
        when(trainerRepository.findByUserName(USERNAME)).thenReturn(Optional.of(entity(false)));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.changeProfileStatus(USERNAME, false));

        assertEquals("Trainer profile is already inactive.", exception.getMessage());
        verify(trainerRepository, never()).changeProfileStatus(any(), anyBoolean());
    }

    @Test
    void rejectsNullStatusUsername() {
        assertThrows(NullPointerException.class, () -> service.changeProfileStatus(null, true));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void rejectsStatusChangeForMissingTrainer() {
        assertThrows(EntityNotFoundException.class, () -> service.changeProfileStatus(USERNAME, false));

        verify(trainerRepository, never()).changeProfileStatus(any(), anyBoolean());
    }

    @Test
    void findsTrainingsAndMapsEveryEntity() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, null);
        TrainerTrainingFilter filter = new TrainerTrainingFilter(null, null, null);
        TrainingEntity first = new TrainingEntity();
        TrainingEntity second = new TrainingEntity();
        Training firstResult = Training.builder().id(1L).build();
        Training secondResult = Training.builder().id(2L).build();
        when(criteriaMapper.toFilter(criteria)).thenReturn(filter);
        when(trainingRepository.findByTrainerUsername(USERNAME, filter)).thenReturn(List.of(first, second));
        when(trainingMapper.toTrainingDomain(first)).thenReturn(firstResult);
        when(trainingMapper.toTrainingDomain(second)).thenReturn(secondResult);

        List<Training> result = service.findTrainings("  " + USERNAME + "  ", criteria);

        assertEquals(List.of(firstResult, secondResult), result);
        verify(trainingRepository).findByTrainerUsername(USERNAME, filter);
    }

    @Test
    void returnsEmptyTrainingList() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, null);
        TrainerTrainingFilter filter = new TrainerTrainingFilter(null, null, null);
        when(criteriaMapper.toFilter(criteria)).thenReturn(filter);
        when(trainingRepository.findByTrainerUsername(USERNAME, filter)).thenReturn(List.of());

        List<Training> result = service.findTrainings(USERNAME, criteria);

        assertTrue(result.isEmpty());
        verifyNoInteractions(trainingMapper);
    }

    @Test
    void rejectsNullTrainingSearchUsername() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, null);

        assertThrows(IncorrectMethodArgumentException.class, () -> service.findTrainings(null, criteria));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void rejectsBlankTrainingSearchUsername() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, null);

        assertThrows(IncorrectMethodArgumentException.class, () -> service.findTrainings("  ", criteria));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void rejectsNullTrainingSearchCriteria() {
        assertThrows(NullPointerException.class, () -> service.findTrainings(USERNAME, null));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void findsUnassignedTrainers() {
        TrainerEntity first = entity(true);
        TrainerEntity second = entity(false);
        Trainer firstResult = trainer();
        Trainer secondResult = trainer();
        when(trainerRepository.findUnassignedTrainers("james.bond")).thenReturn(List.of(first, second));
        when(trainerMapper.toTrainerDomain(first)).thenReturn(firstResult);
        when(trainerMapper.toTrainerDomain(second)).thenReturn(secondResult);

        List<Trainer> result = service.getUnassignedTrainers("james.bond");

        assertEquals(List.of(firstResult, secondResult), result);
        verify(trainerMapper).toTrainerDomain(first);
        verify(trainerMapper).toTrainerDomain(second);
    }

    @Test
    void returnsEmptyUnassignedTrainers() {
        when(trainerRepository.findUnassignedTrainers("james.bond")).thenReturn(List.of());

        List<Trainer> result = service.getUnassignedTrainers("james.bond");

        assertTrue(result.isEmpty());
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void rejectsNullUnassignedTraineeUsername() {
        assertThrows(NullPointerException.class, () -> service.getUnassignedTrainers(null));

        verifyNoInteractions(trainerRepository);
    }

    @Test
    void delegatesPasswordChange() {
        String oldPassword = "old";
        String newPassword = "new";

        service.changePassword(USERNAME, oldPassword, newPassword);

        verify(credentialService).changePassword(USERNAME, oldPassword, newPassword);
        verifyNoMoreInteractions(credentialService);
    }
}
