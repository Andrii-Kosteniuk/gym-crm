package com.training.gym.service;

import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.dao.UserRepository;
import com.training.gym.dto.trainee.TraineeTrainingCriteria;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.model.Trainee;
import com.training.gym.model.Trainer;
import com.training.gym.model.Training;
import com.training.gym.model.User;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    public static final String TRAINEE_NOT_FOUND = "Trainee not found: ";
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;

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

        User updatedUser = Objects.requireNonNull(trainee.getUser(), "User must not be null");

        String username = updatedUser.getUserName();

        Trainee existing = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));

        User existingUser = existing.getUser();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        existing.setDateOfBirth(trainee.getDateOfBirth());
        existing.setAddress(trainee.getAddress());

        log.info("Trainee updated: username={}", username);

        return existing;
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

        for (Trainer trainer : new HashSet<>(trainee.getTrainers())) {
            trainee.removeTrainer(trainer);
        }

        traineeRepository.delete(trainee);

        userRepository.delete(user);

        log.info("Trainee deleted: username={}", username);
    }


    @Override
    @Transactional
    public void activateTrainee(Trainee trainee) {
        Trainee existing = getTraineeById(Objects.requireNonNull(trainee).getId());

        if (Boolean.TRUE.equals(existing.getUser().getActive())) {
            throw new IllegalStateException("Trainee is already active");
        }

        existing.getUser().setActive(true);
        log.info("Trainee activated: id={}", existing.getId());
    }

    @Override
    @Transactional
    public void deactivateTrainee(Trainee trainee) {
        Trainee existing = getTraineeById(Objects.requireNonNull(trainee).getId());

        if (Boolean.FALSE.equals(existing.getUser().getActive())) {
            throw new IllegalStateException("Trainee is already inactive");
        }

        existing.getUser().setActive(false);
        log.info("Trainee deactivated: id={}", existing.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findTrainings(String username, TraineeTrainingCriteria criteria) {
        Objects.requireNonNull(criteria, "Criteria must not be null");
        log.debug("Finding training by specified criteria");

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Trainee username must not be blank");
        }

        return trainingRepository.findByTraineeUsername(username, criteria);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        credentialService.changePassword(username, oldPassword, newPassword);
    }

    @Override
    @Transactional
    public Set<Trainer> replaceTrainers(String username, Set<Long> ids) {
        Objects.requireNonNull(ids, "Update DTO is required");

        if (username == null
                || username.isBlank()
                || ids.stream()
                .anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Invalid trainer assignment request");
        }

        Set<Trainer> trainers = new HashSet<>();

        for (Long trainerId : ids) {
            Trainer trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerId));

            trainers.add(trainer);
        }

        Trainee trainee = findByUserName(username);

        Set<Trainer> currentTrainers = new HashSet<>(trainee.getTrainers());

        for (Trainer trainer : currentTrainers) {
            if (!trainers.contains(trainer)) {
                trainee.removeTrainer(trainer);
            }
        }

        for (Trainer trainer : trainers) {
            if (!currentTrainers.contains(trainer)) {
                trainee.assignTrainer(trainer);
            }
        }

        log.info("Updated trainer assignments for trainee with ID={}", trainee.getId());

        return Set.copyOf(trainee.getTrainers());
    }

}
