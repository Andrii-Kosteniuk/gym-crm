package com.training.gym.service.training;

import com.training.gym.domain.model.Trainee;
import com.training.gym.domain.model.Trainer;
import com.training.gym.domain.model.Training;
import com.training.gym.domain.model.TrainingType;
import com.training.gym.exception.BusinessRuleViolationException;
import com.training.gym.exception.EntityNotFoundException;
import com.training.gym.exception.IncorrectMethodArgumentException;
import com.training.gym.persistence.entity.TraineeEntity;
import com.training.gym.persistence.entity.TrainerEntity;
import com.training.gym.persistence.entity.TrainingEntity;
import com.training.gym.persistence.entity.TrainingTypeEntity;
import com.training.gym.repository.TraineeRepository;
import com.training.gym.repository.TrainerRepository;
import com.training.gym.repository.TrainingRepository;
import com.training.gym.repository.TrainingTypeRepository;
import com.training.gym.service.mapper.TraineePersistenceMapper;
import com.training.gym.service.mapper.TrainerPersistenceMapper;
import com.training.gym.service.mapper.TrainingPersistenceMapper;
import com.training.gym.service.mapper.TrainingTypePersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingPersistenceMapper trainingMapper;
    private final TraineePersistenceMapper traineeMapper;
    private final TrainerPersistenceMapper trainerMapper;
    private final TrainingTypePersistenceMapper trainingTypeMapper;

    @Override
    @Transactional
    public Training createTraining(Training training) {
        log.debug("Creating training");

        validateTrainingReferences(training);


        TraineeEntity trainee = traineeRepository.findById(training.getTrainee().getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found"));

        TrainerEntity trainer = trainerRepository.findById(training.getTrainer().getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found"));


        TrainingTypeEntity trainingType = trainingTypeRepository.findById(training.getTrainingType().getId())
                .orElseThrow(() -> new EntityNotFoundException("Training type not found"));

        Trainee traineeDomain = traineeMapper.toTraineeDomain(trainee);
        Trainer trainerDomain = trainerMapper.toTrainerDomain(trainer);
        TrainingType trainingTypeDomain = trainingTypeMapper.toTrainingTypeDomain(trainingType);

        validateParticipants(traineeDomain, trainerDomain);
        validateSpecialization(training, trainerDomain);

        training.setTrainee(traineeDomain);
        training.setTrainer(trainerDomain);
        training.setTrainingType(trainingTypeDomain);

        TrainingEntity trainingEntity = trainingMapper.toTrainingEntity(training);

        TrainingEntity savedTraining = trainingRepository.save(trainingEntity);

        log.info("Training created: id={}", savedTraining.getId());
        return trainingMapper.toTrainingDomain(savedTraining);
    }

    @Override
    @Transactional(readOnly = true)
    public Training getTrainingById(Long id) {
        log.debug("Retrieving training: id={}", id);

        TrainingEntity trainingEntity = trainingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training with id %d not found".formatted(id)));

        return trainingMapper.toTrainingDomain(trainingEntity);
    }

    private void validateParticipants(Trainee trainee, Trainer trainer) {
        if (!Boolean.TRUE.equals(trainee.getUser().getActive())) {
            throw new BusinessRuleViolationException("Cannot create training for inactive trainee");
        }

        if (!Boolean.TRUE.equals(trainer.getUser().getActive())) {
            throw new BusinessRuleViolationException("Cannot create training with inactive trainer");
        }
    }

    private void validateSpecialization(Training training, Trainer trainer) {
        Objects.requireNonNull(training, "Training  must not be null");
        Objects.requireNonNull(trainer, "Trainer  must not be null");

        if (!Objects.equals(training.getTrainingType().getId(), trainer.getSpecialization().getId())) {
            throw new BusinessRuleViolationException("Training type does not match trainer specialization");
        }
    }

    private void validateTrainingReferences(Training training) {
        Objects.requireNonNull(training, "Training is required");

        if (training.getId() != null) {
            throw new IncorrectMethodArgumentException("New training must not have an ID");
        }

        if (training.getTrainee() == null
                || training.getTrainee().getId() == null
                || training.getTrainee().getId() <= 0) {
            throw new IncorrectMethodArgumentException("Valid trainee ID is required");
        }

        if (training.getTrainer() == null
                || training.getTrainer().getId() == null
                || training.getTrainer().getId() <= 0) {
            throw new IncorrectMethodArgumentException("Valid trainer ID is required");
        }

        if (training.getTrainingType() == null
                || training.getTrainingType().getId() == null
                || training.getTrainingType().getId() <= 0) {
            throw new IncorrectMethodArgumentException("Valid training type ID is required");
        }

        if (training.getTrainingDate().isBefore(LocalDate.now(Clock.systemDefaultZone()))) {
            throw new IncorrectMethodArgumentException("Training date is in past");
        }
    }
}