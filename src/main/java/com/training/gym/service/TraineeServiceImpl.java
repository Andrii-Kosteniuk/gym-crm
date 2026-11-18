package com.training.gym.service;

import com.training.gym.domain.Trainee;
import com.training.gym.domain.Trainer;
import com.training.gym.domain.Training;
import com.training.gym.domain.User;
import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
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
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    public static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final TrainingCriteriaMapper criteriaMapper;

    @Override
    @Transactional
    public RegistrationResult<Trainee> registerTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee, "Trainee must not be null");


        User user = Objects.requireNonNull(trainee.getUser(), "User must not be null");


        String username = usernameGenerator.generate(
                user.getFirstName(),
                user.getLastName(),
                userRepository::existsByUsername
        );

        String rawPassword = passwordGenerator.generate();

        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        Trainee saved = traineeRepository.save(trainee);

        log.info("Trainee registered: username={}", username);

        return new RegistrationResult<>(saved, rawPassword);
    }

    @Override
    @Transactional
    public Trainee updateTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee, "Trainee must not be null");

        User user = Objects.requireNonNull(trainee.getUser());

        String username = user.getUserName();

        Trainee existingTrainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));

        existingTrainee.getUser().setFirstName(user.getFirstName());
        existingTrainee.getUser().setLastName(user.getLastName());

        Trainee updatedTrainee = traineeRepository.update(existingTrainee);

        log.info("Trainee updated: username={}", username);

        return updatedTrainee;
    }

    @Override
    public Trainee getTraineeById(Long id) {
        Objects.requireNonNull(id, "Id can not be null");
        log.debug("Retrieving trainee profile: id={}", id);

        return traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with id %d not found".formatted(id)));
    }

    @Override
    public Trainee findByUserName(String username) {
        Objects.requireNonNull(username, "Username can not be null");
        return traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username %s not found".formatted(username)));
    }


    @Override
    @Transactional
    public void removeTraineeByUsername(String username) {
        Trainee trainee = findByUserName(username);

        User user = trainee.getUser();

        traineeRepository.delete(trainee);

        userRepository.delete(user);

        log.info("Trainee deleted: username={}", username);
    }


    @Override
    @Transactional
    public void activateTrainee(Trainee trainee) {
        log.debug("Training to activate trainee profile: id={}", trainee.getId());
        Objects.requireNonNull(trainee);

        Trainee existing = getTraineeById(trainee.getId());

        if (Boolean.TRUE.equals(existing.getUser().getActive())) {
            throw new IllegalStateException("Trainee is already active");
        }

        traineeRepository.activateTrainee(trainee);
        log.info("Trainee activated: id={}", existing.getId());
    }

    @Override
    @Transactional
    public void deactivateTrainee(Trainee trainee) {
        log.debug("Training to deactivate trainee profile: id={}", trainee.getId());
        Objects.requireNonNull(trainee);

        Trainee existing = getTraineeById(trainee.getId());

        if (Boolean.FALSE.equals(existing.getUser().getActive())) {
            throw new IllegalStateException("Trainee is already inactive");
        }

        traineeRepository.deactivateTrainee(trainee);
        log.info("Trainee deactivated: id={}", existing.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findTrainings(String username, TraineeTrainingCriteria criteria) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Trainee username must not be blank");
        }

        Objects.requireNonNull(criteria, "Criteria must not be null");

        TraineeTrainingFilter filter = criteriaMapper.toFilter(criteria);

        return trainingRepository.findByTraineeUsername(username.trim(), filter);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        credentialService.changePassword(username, oldPassword, newPassword);
    }

    @Override
    @Transactional
    public void replaceTrainers(Long traineeId, Set<Long> trainerIds) {
        Objects.requireNonNull(traineeId, "Trainee ID is required");
        Objects.requireNonNull(trainerIds, "Trainer IDs are required");

        if (trainerIds
                .stream()
                .anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Trainer IDs must not contain null");
        }

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        if (Boolean.FALSE.equals(trainee.getUser().getActive())) {
            throw new BusinessRuleViolationException("Cannot assign trainers to inactive trainee");
        }

        for (Long trainerId : trainerIds) {
            Trainer trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerId));

            if (Boolean.FALSE.equals(trainer.getUser().getActive())) {
                throw new BusinessRuleViolationException("Cannot assign inactive trainer: " + trainerId);
            }
        }

        traineeRepository.replaceTrainers(traineeId, trainerIds);

    }

}
