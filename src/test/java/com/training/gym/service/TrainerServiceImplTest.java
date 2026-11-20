package com.training.gym.service;

import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.domain.model.User;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
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
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import com.training.gym.service.mapper.TrainingPersistenceMapper;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {
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

    private Trainer domain() {
        return Trainer.builder().id(2L).user(User.builder().firstName("Sara").lastName("Connor")
                .userName("sara.connor").build()).specialization(TrainingType.builder().id(3L).build()).build();
    }

    private TrainerEntity entity(boolean active) {
        UserEntity user = new UserEntity();
        user.setActive(active);
        user.setFirstName("Sara");
        user.setLastName("Connor");
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(2L);
        trainer.setUserEntity(user);
        return trainer;
    }

    @Test
    void registrationConvertsSpecializationAndPersistsEntity() {
        Trainer trainer = domain();
        TrainerEntity trainerEntity = entity(true);
        Trainer domainTrainer = domain();
        TrainingTypeEntity typeEntity = new TrainingTypeEntity();
        typeEntity.setId(3L);
        TrainingType typeDomain = TrainingType.builder().id(3L).name("Fitness").build();
        when(usernameGenerator.generate(eq("Sara"), eq("Connor"), any())).thenReturn("sara.connor");
        when(passwordGenerator.generate()).thenReturn("secret");
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(typeEntity));
        when(trainingTypeMapper.toTrainingTypeDomain(typeEntity)).thenReturn(typeDomain);
        when(trainerMapper.toEntity(trainer)).thenReturn(trainerEntity);
        when(trainerRepository.save(trainerEntity)).thenReturn(trainerEntity);
        when(trainerMapper.toTrainerDomain(trainerEntity)).thenReturn(domainTrainer);
        var result = service.registerTrainer(trainer);
        assertSame(domainTrainer, result.profile());
        assertEquals("secret", result.initialPassword());
        assertSame(typeDomain, trainer.getSpecialization());
        assertEquals("hash", trainer.getUser().getPassword());
        verify(trainerRepository).save(trainerEntity);
    }

    @Test
    void registrationRejectsInvalidRequestsAndUnknownSpecialization() {
        assertThrows(NullPointerException.class, () -> service.registerTrainer(null));
        assertThrows(NullPointerException.class, () -> service.registerTrainer(Trainer.builder().build()));
        assertThrows(NullPointerException.class, () -> service.registerTrainer(Trainer.builder().user(User.builder().build()).build()));
        when(usernameGenerator.generate(eq("Sara"), eq("Connor"), any())).thenReturn("sara.connor");
        assertThrows(EntityNotFoundException.class, () -> service.registerTrainer(domain()));
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateModifiesManagedEntityThenMapsToDomain() {
        Trainer trainer = domain();
        TrainerEntity existing = entity(true);
        Trainer domain = domain();
        TrainingTypeEntity specialization = new TrainingTypeEntity();
        specialization.setId(3L);
        when(trainerRepository.findByUserName("sara.connor")).thenReturn(Optional.of(existing));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(specialization));
        when(trainerRepository.update(existing)).thenReturn(existing);
        when(trainerMapper.toTrainerDomain(existing)).thenReturn(domain);
        assertSame(domain, service.updateTrainer(trainer));
        assertSame(specialization, existing.getSpecialization());
        verify(trainerRepository).update(existing);
    }

    @Test
    void updateRejectsInvalidAndMissingTrainer() {
        assertThrows(NullPointerException.class, () -> service.updateTrainer(null));
        assertThrows(NullPointerException.class, () -> service.updateTrainer(Trainer.builder().build()));
        assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(domain()));
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void lookupsConvertEntitiesToDomain() {
        TrainerEntity saved = entity(true);
        Trainer domain = domain();
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(saved));
        when(trainerRepository.findByUserName("sara.connor")).thenReturn(Optional.of(saved));
        when(trainerMapper.toTrainerDomain(saved)).thenReturn(domain);
        assertSame(domain, service.getTrainerById(2L));
        assertSame(domain, service.findByUserName("sara.connor"));
    }

    @Test
    void missingAndNullLookupsAreRejected() {
        assertThrows(NullPointerException.class, () -> service.getTrainerById(null));
        assertThrows(NullPointerException.class, () -> service.findByUserName(null));
        assertThrows(EntityNotFoundException.class, () -> service.getTrainerById(2L));
        assertThrows(EntityNotFoundException.class, () -> service.findByUserName("missing"));
    }

    @Test
    void activationChecksStoredStateAndUsesManagedEntities() {
        Trainer trainer = domain();
        TrainerEntity inactive = entity(false);
        TrainerEntity active = entity(true);
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(inactive), Optional.of(active), Optional.of(active), Optional.of(inactive));
        service.activateTrainer(trainer);
        assertThrows(IllegalStateException.class, () -> service.activateTrainer(trainer));
        service.deactivateTrainer(trainer);
        assertThrows(IllegalStateException.class, () -> service.deactivateTrainer(trainer));
        verify(trainerRepository).activateTrainer(inactive);
        verify(trainerRepository).deactivateTrainer(active);
    }

    @Test
    void activationRejectsInvalidAndMissingProfiles() {
        assertThrows(NullPointerException.class, () -> service.activateTrainer(null));
        assertThrows(NullPointerException.class, () -> service.deactivateTrainer(null));
        assertThrows(EntityNotFoundException.class, () -> service.activateTrainer(domain()));
        assertThrows(EntityNotFoundException.class, () -> service.deactivateTrainer(domain()));
    }

    @Test
    void searchAndUnassignedListMapEntities() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, null);
        TrainerTrainingFilter filter = new TrainerTrainingFilter(null, null, null);
        TrainingEntity training = new TrainingEntity();
        Training result = Training.builder().build();
        when(criteriaMapper.toFilter(criteria)).thenReturn(filter);
        when(trainingRepository.findByTrainerUsername("sara.connor", filter)).thenReturn(List.of(training));
        when(trainingMapper.toTrainingDomain(training)).thenReturn(result);
        assertEquals(List.of(result), service.findTrainings(" sara.connor ", criteria));
        TrainerEntity stored = entity(true);
        Trainer trainer = domain();
        when(trainerRepository.findUnassignedTrainers("james.bond")).thenReturn(List.of(stored));
        when(trainerMapper.toTrainerDomain(stored)).thenReturn(trainer);
        assertEquals(List.of(trainer), service.getUnassignedTrainers("james.bond"));
    }

    @Test
    void searchRejectsInvalidArguments() {
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(null, null, null);
        assertThrows(IncorrectMethodArgumentException.class, () -> service.findTrainings(" ", criteria));
        assertThrows(NullPointerException.class, () -> service.findTrainings("sara.connor", null));
        assertThrows(NullPointerException.class, () -> service.getUnassignedTrainers(null));
    }

    @Test
    void passwordChangeDelegates() {
        service.changePassword("sara.connor", "old", "new");
        verify(credentialService).changePassword("sara.connor", "old", "new");
    }
}
