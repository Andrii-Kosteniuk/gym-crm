package com.training.gym.service;

import com.training.gym.domain.criteria.TraineeTrainingFilter;
import com.training.gym.domain.mapper.TrainingCriteriaMapper;
import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.User;
import com.training.gym.dto.auth.RegistrationResult;
import com.training.gym.dto.crireria.TraineeTrainingCriteria;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.UserEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.UserRepository;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainingPersistenceMapper;
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

    public static final String TRAINEE_NOT_FOUND_USERNAME = "Trainee with %s username not found";
    public static final String NOT_FOUND_ID = "Trainee with id %d not found";

    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CredentialService credentialService;
    private final TrainingCriteriaMapper criteriaMapper;
    private final TraineePersistenceMapper traineeMapper;
    private final TrainingPersistenceMapper trainingMapper;

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

        TraineeEntity saved = traineeRepository.save(traineeMapper.toEntity(trainee));

        log.info("Trainee registered: username={}", username);

        return new RegistrationResult<>(traineeMapper.toTraineeDomain(saved), rawPassword);
    }

    @Override
    @Transactional
    public Trainee updateTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee, "Trainee must not be null");

        User user = Objects.requireNonNull(trainee.getUser());

        String username = user.getUserName();

        TraineeEntity existingTrainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND_USERNAME.formatted(username)));

        existingTrainee.getUserEntity().setFirstName(user.getFirstName());
        existingTrainee.getUserEntity().setLastName(user.getLastName());
        existingTrainee.setDateOfBirth(trainee.getDateOfBirth());
        existingTrainee.setAddressEntity(traineeMapper.mapAddress(trainee.getAddress()));

        TraineeEntity updatedTrainee = traineeRepository.update(existingTrainee);

        log.info("Trainee updated: username={}", username);

        return traineeMapper.toTraineeDomain(updatedTrainee);
    }

    @Override
    public Trainee getTraineeById(Long id) {
        Objects.requireNonNull(id, "Id can not be null");
        log.debug("Retrieving trainee profile: id={}", id);

        TraineeEntity traineeEntity = traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID.formatted(id)));

        return traineeMapper.toTraineeDomain(traineeEntity);
    }

    @Override
    public Trainee findByUserName(String username) {
        Objects.requireNonNull(username, "Username can not be null");

        TraineeEntity traineeEntity = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username %s not found".formatted(username)));

        return traineeMapper.toTraineeDomain(traineeEntity);
    }


    @Override
    @Transactional
    public void removeTraineeByUsername(String username) {

        TraineeEntity trainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with username %s not found".formatted(username)));

        UserEntity user = trainee.getUserEntity();

        traineeRepository.delete(trainee);

        userRepository.delete(user);

        log.info("Trainee profile has been removed: username={}", username);
    }


    @Override
    @Transactional
    public void activateTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee);
        log.debug("Training to activate trainee profile: id={}", trainee.getId());

        TraineeEntity existing = traineeRepository.findById(trainee.getId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID.formatted(trainee.getId())));

        if (Boolean.TRUE.equals(existing.getUserEntity().getActive())) {
            throw new IllegalStateException("Trainee profile is already active.");
        }

        traineeRepository.activateTrainee(traineeMapper.toEntity(trainee));

        log.info("Trainee activated: id={}", existing.getId());
    }

    @Override
    @Transactional
    public void deactivateTrainee(Trainee trainee) {
        Objects.requireNonNull(trainee);
        log.debug("Training to deactivate trainee profile: id={}", trainee.getId());

        TraineeEntity existing = traineeRepository.findById(trainee.getId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID.formatted(trainee.getId())));

        if (Boolean.FALSE.equals(existing.getUserEntity().getActive())) {
            throw new IllegalStateException("Trainee is already inactive");
        }

        traineeRepository.deactivateTrainee(traineeMapper.toEntity(trainee));

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

        List<TrainingEntity> trainees = trainingRepository.findByTraineeUsername(username.trim(), filter);

        return trainees.stream()
                .map(trainingMapper::toTrainingDomain)
                .toList();
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

        TraineeEntity trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        if (Boolean.FALSE.equals(trainee.getUserEntity().getActive())) {
            throw new BusinessRuleViolationException("Cannot assign trainers to inactive trainee");
        }

        for (Long trainerId : trainerIds) {
            TrainerEntity trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ID.formatted(trainerId)));

            if (Boolean.FALSE.equals(trainer.getUserEntity().getActive())) {
                throw new BusinessRuleViolationException("Cannot assign inactive trainer: " + trainerId);
            }
        }

        traineeRepository.replaceTrainers(traineeId, trainerIds);

    }

}
