package com.training.gym.service;

import com.training.gym.util.IdGenerator;
import com.training.gym.util.PasswordGenerator;
import com.training.gym.util.UsernameGenerator;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.ResourceConflictException;
import com.training.gym.model.Trainee;
import com.training.gym.dto.trainee.CreateTraineeDto;
import com.training.gym.dto.trainee.UpdateTraineeDto;
import com.training.gym.mapper.TraineeMapper;
import com.training.gym.dao.TraineeRepository;
import com.training.gym.dao.TrainerRepository;
import com.training.gym.dao.TrainingRepository;
import com.training.gym.validation.TraineeDtoValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TraineeMapper traineeMapper;
    private final IdGenerator idGenerator;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TraineeDtoValidator traineeValidator;

    public TraineeServiceImpl(TraineeRepository traineeRepository,
                              TrainerRepository trainerRepository,
                              TrainingRepository trainingRepository,
                              TraineeMapper traineeMapper,
                              @Qualifier("traineeIdGenerator") IdGenerator idGenerator,
                              UsernameGenerator usernameGenerator,
                              PasswordGenerator passwordGenerator,
                              TraineeDtoValidator traineeValidator) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.traineeMapper = traineeMapper;
        this.idGenerator = idGenerator;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.traineeValidator = traineeValidator;
    }

    @Override
    public Trainee createTrainee(CreateTraineeDto traineeDto) {
        log.debug("Creating trainee profile");
        Objects.requireNonNull(traineeDto, "Dto cannot be null");

        traineeValidator.validateCreate(traineeDto);

        Trainee trainee = traineeMapper.toTrainee(traineeDto);

        trainee.setId(idGenerator.generate());
        trainee.setPassword(passwordGenerator.generate());

        String uniqUserName = usernameGenerator.generate(traineeDto.firstName(), traineeDto.lastName(), this::usernameExists);
        trainee.setUserName(uniqUserName);
        trainee.setActive(true);

        Trainee savedTrainee = traineeRepository.save(trainee);

        log.info("Trainee profile created successfully: username={}", uniqUserName);
        return savedTrainee;

    }

    @Override
    public Trainee updateTrainee(UpdateTraineeDto traineeDto) {
        Objects.requireNonNull(traineeDto, "Dto cannot be null");
        traineeValidator.validateUpdate(traineeDto);

        String username = traineeDto.userName();

        log.debug("Updating trainee profile: username={}", username);

        Trainee existing = traineeRepository
                .findByUserName(traineeDto.userName())
                .orElseThrow(() -> {
                    log.warn("Trainee update failed: username={} not found", username);
                    return new EntityNotFoundException("Trainee with username %s not found".formatted(traineeDto.userName()));
                });

        traineeMapper.updateTrainee(traineeDto, existing);

        Trainee updatedTrainee = traineeRepository.update(existing);

        log.info("Trainee profile updated successfully: username={}", username);
        return updatedTrainee;
    }

    @Override
    public Trainee getTraineeById(Long id) {
        log.debug("Retrieving trainee profile: id={}", id);
        Objects.requireNonNull(id, "Id cannot be null");
        return traineeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee with id %d not found".formatted(id)));
    }

    @Override
    public void removeTrainee(Long id) {
        Objects.requireNonNull(id, "Trainee ID must not be null");

        log.debug("Deleting trainee profile: id={}", id);

        if (!traineeRepository.existsById(id)) {
            log.error("Failed to find trainee by id {}", id);

            throw new EntityNotFoundException(
                    "Trainee with id %d not found".formatted(id)
            );
        }

        if (trainingRepository.existsByTraineeId(id)) {
            log.warn(
                    "Trainee deletion rejected because related trainings exist: id={}",
                    id
            );

            throw new ResourceConflictException(
                    "Cannot delete trainee with id %d because related trainings exist"
                            .formatted(id)
            );
        }

        traineeRepository.deleteById(id);
        log.info("Trainee profile deleted successfully: id={}", id);
    }

    private boolean usernameExists(String username) {
        return traineeRepository.existsByUserName(username)
                || trainerRepository.existsByUserName(username);
    }

}
