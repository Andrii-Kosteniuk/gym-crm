package com.training.gym.service;

import com.training.gym.domain.Trainer;
import com.training.gym.domain.Training;
import com.training.gym.domain.TrainingType;
import com.training.gym.domain.User;
import com.training.gym.domain.criteria.TrainerTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.dto.crireria.TrainerTrainingCriteria;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.TrainingTypeRepository;
import com.training.gym.repository.UserRepository;
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

    public static final String TRAINER_NOT_FOUND = "Trainer not found: ";
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TrainingTypeRepository trainingTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final TrainingCriteriaMapper criteriaMapper;


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

        TrainingType specialization = trainingTypeRepository
                .findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found: " + specializationId));

        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        trainer.setSpecialization(specialization);

        Trainer saved = trainerRepository.save(trainer);

        log.info("Trainer registered: username={}", username);

        return new RegistrationResult<>(saved, rawPassword);
    }

    @Override
    @Transactional
    public Trainer updateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer must not be null");

        User updatedUser = Objects.requireNonNull(trainer.getUser(), "User must not be null");

        String username = updatedUser.getUserName();

        Trainer existing = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINER_NOT_FOUND + username));

        Long specializationId = Objects.requireNonNull(trainer.getSpecialization(), "Specialization is required").getId();

        TrainingType specialization = trainingTypeRepository
                .findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + specializationId
                ));

        User existingUser = existing.getUser();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        existing.setSpecialization(specialization);

        log.info("Trainer updated: username={}", username);

        return trainerRepository.update(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer getTrainerById(Long id) {
        Objects.requireNonNull(id, "Id can not be null");
        log.debug("Retrieving trainer profile: id={}", id);

        return trainerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with id %d not found".formatted(id)));
    }

    @Override
    public Trainer findByUserName(String username) {
        Objects.requireNonNull(username, "Username can not be null");
        return trainerRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer with username %s not found".formatted(username)));
    }

    @Transactional
    public void activateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");

        Trainer managedTrainer = trainerRepository.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException(TRAINER_NOT_FOUND + trainer.getId()));

        if (Boolean.TRUE == managedTrainer.getUser().getActive())
            throw new IllegalStateException("Trainer is already active.");

        trainerRepository.activateTrainer(managedTrainer);
        log.info("Trainer profile has been activated");
    }

    @Override
    @Transactional
    public void deactivateTrainer(Trainer trainer) {
        Objects.requireNonNull(trainer, "Trainer is required");

        Trainer managedTrainer = trainerRepository.findById(trainer.getId())
                .orElseThrow(() -> new EntityNotFoundException(TRAINER_NOT_FOUND + trainer.getId()));

        if (Boolean.FALSE == managedTrainer.getUser().getActive())
            throw new IllegalStateException("Trainer is already deactivated.");

        trainerRepository.deactivateTrainer(managedTrainer);
        log.info("Trainer profile has been deactivated");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findTrainings(String username, TrainerTrainingCriteria criteria) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Trainer username must not be blank");
        }

        Objects.requireNonNull(criteria, "Criteria must not be null");

        TrainerTrainingFilter filter = criteriaMapper.toFilter(criteria);

        return trainingRepository.findByTrainerUsername(username.trim(), filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String username) {
        Objects.requireNonNull(username, "Trainee username must not be null");

        return trainerRepository.findUnassignedTrainers(username);
    }


    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        credentialService.changePassword(username, oldPassword, newPassword);
    }
}
