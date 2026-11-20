package com.training.gym.service;

import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.User;
import com.training.gym.dto.auth.RegistrationResult;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    public static final String NOT_FOUND_ID = "Trainer with ID %d not found";
    public static final String NOT_FOUND_USERNAME = "Trainer with username %s not found";

    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TrainingTypeRepository trainingTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final TrainingCriteriaMapper criteriaMapper;
    private final TrainerPersistenceMapper trainerMapper;
    private final TrainingPersistenceMapper trainingMapper;
    private final TrainingTypePersistenceMapper trainingTypeMapper;


    @Override
    @Transactional
    public RegistrationResult<Trainer> registerTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer must not be null");
        Objects.requireNonNull(trainer.getUser(), "User must not be null");
        Objects.requireNonNull(trainer.getSpecialization(), "Specialization is required");

        User user = trainer.getUser();

        String username = usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                userRepository::existsByUsername
        );

        String rawPassword = passwordGenerator.generate();

        Long specializationId = trainer.getSpecialization().getId();

        TrainingTypeEntity specialization = getSpecialization(specializationId);

        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        trainer.setSpecialization(trainingTypeMapper.toTrainingTypeDomain(specialization));

        TrainerEntity savedTrainer = trainerRepository.save(trainerMapper.toEntity(trainer));

        log.info("Trainer registered: username={}", username);

        return new RegistrationResult<>(trainerMapper.toTrainerDomain(savedTrainer), rawPassword);
    }

    @Override
    @Transactional
    public Trainer updateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer must not be null");

        User updatedUser = Objects.requireNonNull(trainer.getUser(), "User must not be null");

        String username = updatedUser.getUserName();

        TrainerEntity existingTrainer = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID + username));

        Long specializationId = Objects.requireNonNull(trainer.getSpecialization(), "Specialization is required").getId();

        TrainingTypeEntity specialization = getSpecialization(specializationId);

        UserEntity existingUser = existingTrainer.getUserEntity();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingTrainer.setSpecialization(specialization);

        TrainerEntity updatedTrainer = trainerRepository.update(existingTrainer);

        log.info("Trainer updated: username={}", username);

        return trainerMapper.toTrainerDomain(updatedTrainer);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getTrainerById(Long id) {
        Objects.requireNonNull(id, "Id can not be null");
        log.debug("Retrieving trainer profile: id={}", id);

        TrainerEntity trainerEntity = trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID.formatted(id)));

        return trainerMapper.toTrainerDomain(trainerEntity);
    }

    @Override
    public Trainer findByUserName(String username) {
        Objects.requireNonNull(username, "Username can not be null");

        TrainerEntity trainerEntity = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_USERNAME.formatted(username)));

        return trainerMapper.toTrainerDomain(trainerEntity);
    }

    @Transactional
    public void activateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");

        TrainerEntity managedTrainer = trainerRepository.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID.formatted(trainer.getId())));

        if (Boolean.TRUE == managedTrainer.getUserEntity().getActive())
            throw new IllegalStateException("Trainer is already active.");

        trainerRepository.activateTrainer(managedTrainer);
        log.info("Trainer profile has been activated");
    }

    @Override
    @Transactional
    public void deactivateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");

        TrainerEntity managedTrainer = trainerRepository.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID + trainer.getId()));

        if (Boolean.FALSE == managedTrainer.getUserEntity().getActive())
            throw new IllegalStateException("Trainer is already deactivated.");

        trainerRepository.deactivateTrainer(managedTrainer);
        log.info("Trainer profile has been deactivated");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findTrainings(String username, TrainerTrainingCriteria criteria) {

        if (username == null || username.isBlank()) {
            throw new IncorrectMethodArgumentException("Trainer username must not be blank");
        }

        Objects.requireNonNull(criteria, "Criteria must not be null");

        TrainerTrainingFilter filter = criteriaMapper.toFilter(criteria);

        List<TrainingEntity> trainers = trainingRepository.findByTrainerUsername(username.trim(), filter);

        return trainers.stream()
                .map(trainingMapper::toTrainingDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String username) {
        Objects.requireNonNull(username, "Trainee username must not be null");

        List<TrainerEntity> trainers = trainerRepository.findUnassignedTrainers(username);

        return trainers.stream()
                .map(trainerMapper::toTrainerDomain)
                .toList();
    }


    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        credentialService.changePassword(username, oldPassword, newPassword);
    }

    private TrainingTypeEntity getSpecialization(Long specializationId) {
        return trainingTypeRepository
                .findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));

    }
}
